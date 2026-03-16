"use client";

import { useState, useEffect, useRef, useCallback } from "react";
import api from "@/services/api";
import { useAuth } from "@/context/AuthContext";

/* ─── helpers ─────────────────────────────────────────────────────── */
function fmt(ts) {
    if (!ts) return "";
    const d = new Date(ts), now = new Date();
    const m = Math.floor((now - d) / 60000);
    if (m < 1) return "just now";
    if (m < 60) return `${m}m ago`;
    if (m < 1440) return `${Math.floor(m / 60)}h ago`;
    return d.toLocaleDateString();
}

/* ─── sub-components ──────────────────────────────────────────────── */
function Pill({ children, color = "bg-teal-500" }) {
    return (
        <span className={`text-xs font-bold text-white ${color} rounded-full px-2 py-0.5`}>
            {children}
        </span>
    );
}

function RoomRow({ room, selected, onClick }) {
    const isGroup = room.type === "group" || room.type === "GROUP";
    return (
        <button
            onClick={onClick}
            className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-left transition-all
                ${selected
                    ? "bg-teal-600 text-white"
                    : "hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-800 dark:text-gray-200"}`}
        >
            {/* Avatar */}
            <div className={`w-9 h-9 rounded-full flex items-center justify-center flex-shrink-0 font-bold text-sm
                ${selected ? "bg-teal-400 text-white" : "bg-teal-100 dark:bg-teal-900 text-teal-700 dark:text-teal-300"}`}>
                {isGroup ? "👥" : "💬"}
            </div>
            <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between">
                    <span className="font-medium text-sm truncate">{room.name}</span>
                    {room.unreadCount > 0 && (
                        <Pill color={selected ? "bg-white text-teal-600" : "bg-teal-500"}>
                            {room.unreadCount > 99 ? "99+" : room.unreadCount}
                        </Pill>
                    )}
                </div>
                <p className={`text-xs truncate mt-0.5 ${selected ? "text-teal-200" : "text-gray-500 dark:text-gray-400"}`}>
                    {room.memberCount != null ? `${room.memberCount} member${room.memberCount !== 1 ? "s" : ""}` : ""}
                </p>
            </div>
        </button>
    );
}

function MemberRow({ member }) {
    return (
        <div className="flex items-center gap-2 py-1.5 px-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700">
            <div className="w-7 h-7 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center text-xs font-bold text-gray-600 dark:text-gray-300">
                {member.userId?.toString().slice(-2)}
            </div>
            <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-800 dark:text-gray-200 truncate">
                    User #{member.userId}
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-400">{member.role}</p>
            </div>
        </div>
    );
}

/* ─── main page ────────────────────────────────────────────────────── */
export default function ChatPage() {
    const { user } = useAuth();
    const userId = user?.id;

    const [rooms, setRooms] = useState([]);
    const [selected, setSelected] = useState(null);
    const [messages, setMessages] = useState([]);
    const [members, setMembers] = useState([]);
    const [input, setInput] = useState("");
    const [sending, setSending] = useState(false);
    const [loadingMsgs, setLoadingMsgs] = useState(false);
    const [error, setError] = useState("");

    // Create-room modal
    const [showCreate, setShowCreate] = useState(false);
    const [newName, setNewName] = useState("");
    const [newType, setNewType] = useState("group");
    const [creating, setCreating] = useState(false);

    // Add-member modal
    const [showAddMember, setShowAddMember] = useState(false);
    const [addMemberIds, setAddMemberIds] = useState("");

    const messagesEndRef = useRef(null);
    const stompRef = useRef(null);
    const subRef = useRef(null);

    /* ── load rooms ─────────────────────────────────────────────── */
    const loadRooms = useCallback(async () => {
        try {
            const { data } = await api.get("/chat/rooms");
            const list = Array.isArray(data) ? data : [];
            setRooms(list);
            if (!selected && list.length > 0) setSelected(list[0]);
        } catch {
            setError("Could not load rooms.");
        }
    }, [selected]);

    useEffect(() => { loadRooms(); }, []);

    /* ── load messages + members when room changes ──────────────── */
    useEffect(() => {
        if (!selected) return;
        setLoadingMsgs(true);
        setMessages([]);
        setMembers([]);

        Promise.all([
            api.get(`/chat/rooms/${selected.id}/messages`),
            api.get(`/chat/rooms/${selected.id}/members`),
        ]).then(([msgRes, memRes]) => {
            setMessages(Array.isArray(msgRes.data) ? msgRes.data : []);
            setMembers(Array.isArray(memRes.data) ? memRes.data : []);
            loadRooms(); // refresh unread counts
        }).catch(() => setError("Failed to load messages."))
            .finally(() => setLoadingMsgs(false));
    }, [selected?.id]);

    /* ── auto-scroll to bottom ──────────────────────────────────── */
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages]);

    /* ── STOMP WebSocket subscription ───────────────────────────── */
    useEffect(() => {
        if (!selected) return;
        let active = true;

        const connectStomp = async () => {
            try {
                const SockJS = (await import("sockjs-client")).default;
                const { Client } = await import("@stomp/stompjs");

                if (stompRef.current) {
                    subRef.current?.unsubscribe();
                    stompRef.current.deactivate();
                }

                const token = localStorage.getItem("token");
                const client = new Client({
                    webSocketFactory: () => new SockJS("http://localhost:8081/ws"),
                    connectHeaders: { Authorization: `Bearer ${token}` },
                    onConnect: () => {
                        if (!active) return;
                        subRef.current = client.subscribe(
                            `/topic/chat/${selected.id}`,
                            (frame) => {
                                const msg = JSON.parse(frame.body);
                                setMessages(prev => [...prev, msg]);
                            }
                        );
                    },
                    onStompError: () => { },
                    reconnectDelay: 5000,
                });
                client.activate();
                stompRef.current = client;
            } catch { /* SockJS not available → REST-only mode */ }
        };

        connectStomp();
        return () => {
            active = false;
            subRef.current?.unsubscribe();
        };
    }, [selected?.id]);

    /* ── send message ───────────────────────────────────────────── */
    const handleSend = async () => {
        if (!input.trim() || !selected || sending) return;
        const content = input.trim();
        setInput("");
        setSending(true);
        try {
            const { data } = await api.post(`/chat/rooms/${selected.id}/messages`, { content });
            setMessages(prev => [...prev, data]);
        } catch (e) {
            setError(e?.response?.data?.message || "Failed to send message");
        } finally { setSending(false); }
    };

    /* ── create room ────────────────────────────────────────────── */
    const handleCreateRoom = async () => {
        if (!newName.trim()) return;
        setCreating(true);
        try {
            const { data } = await api.post("/chat/rooms", { name: newName, type: newType });
            setRooms(prev => [data, ...prev]);
            setSelected(data);
            setShowCreate(false);
            setNewName("");
        } catch (e) {
            setError(e?.response?.data?.message || "Failed to create room");
        } finally { setCreating(false); }
    };

    /* ── add members ─────────────────────────────────────────────── */
    const handleAddMembers = async () => {
        const ids = addMemberIds.split(",").map(s => parseInt(s.trim(), 10)).filter(Boolean);
        if (!ids.length) return;
        try {
            await api.post(`/chat/rooms/${selected.id}/members`, { userIds: ids });
            const { data } = await api.get(`/chat/rooms/${selected.id}/members`);
            setMembers(Array.isArray(data) ? data : []);
            setShowAddMember(false);
            setAddMemberIds("");
        } catch (e) {
            setError(e?.response?.data?.message || "Failed to add members");
        }
    };

    /* ── render ──────────────────────────────────────────────────── */
    return (
        <div className="flex h-[calc(100vh-120px)] bg-white dark:bg-gray-900 rounded-2xl border border-gray-200 dark:border-gray-700 overflow-hidden shadow-sm">

            {/* ── LEFT: Rooms panel ─────────────────────────────── */}
            <aside className="w-64 flex-shrink-0 border-r border-gray-200 dark:border-gray-700 flex flex-col bg-gray-50 dark:bg-gray-800">
                {/* Header */}
                <div className="flex items-center justify-between px-4 py-3 border-b border-gray-200 dark:border-gray-700">
                    <h2 className="font-semibold text-sm text-gray-900 dark:text-white">💬 Rooms</h2>
                    <button
                        onClick={() => setShowCreate(true)}
                        className="text-teal-600 dark:text-teal-400 hover:text-teal-800 font-bold text-lg leading-none"
                        title="New room"
                    >+</button>
                </div>

                {/* Room list */}
                <div className="flex-1 overflow-y-auto p-2 space-y-1">
                    {rooms.length === 0 ? (
                        <p className="text-xs text-gray-400 text-center mt-8 px-4">
                            No rooms yet.<br />Click + to create one.
                        </p>
                    ) : (
                        rooms.map(r => (
                            <RoomRow
                                key={r.id}
                                room={r}
                                selected={selected?.id === r.id}
                                onClick={() => setSelected(r)}
                            />
                        ))
                    )}
                </div>
            </aside>

            {/* ── CENTER: Messages panel ────────────────────────── */}
            <main className="flex-1 flex flex-col min-w-0">
                {selected ? (
                    <>
                        {/* Chat header */}
                        <div className="px-5 py-3 border-b border-gray-200 dark:border-gray-700 flex items-center gap-3 bg-white dark:bg-gray-900">
                            <div className="w-8 h-8 rounded-full bg-teal-100 dark:bg-teal-900 flex items-center justify-center text-sm">
                                {selected.type === "direct" ? "💬" : "👥"}
                            </div>
                            <div>
                                <p className="font-semibold text-sm text-gray-900 dark:text-white">{selected.name}</p>
                                <p className="text-xs text-gray-500 dark:text-gray-400">
                                    {members.length} member{members.length !== 1 ? "s" : ""}
                                </p>
                            </div>
                        </div>

                        {/* Error banner */}
                        {error && (
                            <div className="bg-red-50 dark:bg-red-900/20 border-b border-red-200 dark:border-red-800 px-4 py-2 text-xs text-red-700 dark:text-red-400 flex justify-between">
                                {error}
                                <button onClick={() => setError("")}>✕</button>
                            </div>
                        )}

                        {/* Messages */}
                        <div className="flex-1 overflow-y-auto px-5 py-4 space-y-3">
                            {loadingMsgs ? (
                                <div className="flex items-center justify-center h-full">
                                    <div className="w-6 h-6 border-2 border-teal-500 border-t-transparent rounded-full animate-spin" />
                                </div>
                            ) : messages.length === 0 ? (
                                <div className="flex flex-col items-center justify-center h-full text-gray-400 gap-2">
                                    <span className="text-4xl">💬</span>
                                    <p className="text-sm">No messages yet. Say hello!</p>
                                </div>
                            ) : (
                                messages.map((msg, i) => {
                                    const isMine = msg.senderId === userId;
                                    return (
                                        <div key={msg.id ?? i} className={`flex ${isMine ? "justify-end" : "justify-start"}`}>
                                            <div className={`max-w-[70%] rounded-2xl px-4 py-2 shadow-sm
                                                ${isMine
                                                    ? "bg-teal-600 text-white rounded-br-sm"
                                                    : "bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-gray-100 rounded-bl-sm"}`}>
                                                {!isMine && (
                                                    <p className="text-xs font-semibold text-teal-600 dark:text-teal-400 mb-0.5">
                                                        User #{msg.senderId}
                                                    </p>
                                                )}
                                                <p className="text-sm leading-relaxed break-words">{msg.message}</p>
                                                <p className={`text-xs mt-1 ${isMine ? "text-teal-200" : "text-gray-400 dark:text-gray-500"}`}>
                                                    {fmt(msg.createdAt)}
                                                </p>
                                            </div>
                                        </div>
                                    );
                                })
                            )}
                            <div ref={messagesEndRef} />
                        </div>

                        {/* Input bar */}
                        <div className="px-4 py-3 border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 flex gap-2">
                            <input
                                value={input}
                                onChange={e => setInput(e.target.value)}
                                onKeyDown={e => e.key === "Enter" && !e.shiftKey && handleSend()}
                                placeholder="Type a message... (Enter to send)"
                                className="flex-1 px-4 py-2 rounded-xl border border-gray-200 dark:border-gray-600 bg-gray-50 dark:bg-gray-800 text-sm text-gray-900 dark:text-gray-100 outline-none focus:border-teal-500 dark:focus:border-teal-400 transition"
                            />
                            <button
                                onClick={handleSend}
                                disabled={sending || !input.trim()}
                                className="px-4 py-2 bg-teal-600 text-white rounded-xl font-semibold text-sm disabled:opacity-50 hover:bg-teal-700 transition"
                            >
                                {sending ? "…" : "Send"}
                            </button>
                        </div>
                    </>
                ) : (
                    <div className="flex-1 flex flex-col items-center justify-center gap-4 text-gray-400">
                        <span className="text-5xl">💬</span>
                        <p className="text-base font-medium">Select a room to start chatting</p>
                        <button
                            onClick={() => setShowCreate(true)}
                            className="px-4 py-2 bg-teal-600 text-white rounded-xl text-sm font-semibold hover:bg-teal-700 transition"
                        >
                            + Create Room
                        </button>
                    </div>
                )}
            </main>

            {/* ── RIGHT: Members panel ──────────────────────────── */}
            {selected && (
                <aside className="w-52 flex-shrink-0 border-l border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 flex flex-col">
                    <div className="px-4 py-3 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
                        <h3 className="text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">
                            Members ({members.length})
                        </h3>
                        <button
                            onClick={() => setShowAddMember(true)}
                            className="text-teal-600 dark:text-teal-400 text-lg font-bold leading-none"
                            title="Add member"
                        >+</button>
                    </div>
                    <div className="flex-1 overflow-y-auto p-2 space-y-0.5">
                        {members.map(m => <MemberRow key={m.id} member={m} />)}
                    </div>
                </aside>
            )}

            {/* ── CREATE ROOM MODAL ─────────────────────────────── */}
            {showCreate && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
                    <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl w-full max-w-sm mx-4 p-6">
                        <h3 className="font-bold text-lg text-gray-900 dark:text-white mb-4">Create Room</h3>
                        <input
                            autoFocus
                            value={newName}
                            onChange={e => setNewName(e.target.value)}
                            placeholder="Room name"
                            className="w-full px-4 py-2 border border-gray-200 dark:border-gray-600 rounded-xl bg-gray-50 dark:bg-gray-700 text-sm text-gray-900 dark:text-gray-100 outline-none focus:border-teal-500 mb-3"
                        />
                        <select
                            value={newType}
                            onChange={e => setNewType(e.target.value)}
                            className="w-full px-4 py-2 border border-gray-200 dark:border-gray-600 rounded-xl bg-gray-50 dark:bg-gray-700 text-sm text-gray-900 dark:text-gray-100 outline-none mb-4"
                        >
                            <option value="group">Group</option>
                            <option value="direct">Direct</option>
                        </select>
                        <div className="flex gap-2">
                            <button
                                onClick={() => { setShowCreate(false); setNewName(""); }}
                                className="flex-1 py-2 rounded-xl border border-gray-200 dark:border-gray-600 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition"
                            >Cancel</button>
                            <button
                                onClick={handleCreateRoom}
                                disabled={creating || !newName.trim()}
                                className="flex-1 py-2 bg-teal-600 text-white rounded-xl text-sm font-semibold disabled:opacity-50 hover:bg-teal-700 transition"
                            >{creating ? "Creating…" : "Create"}</button>
                        </div>
                    </div>
                </div>
            )}

            {/* ── ADD MEMBER MODAL ──────────────────────────────── */}
            {showAddMember && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
                    <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl w-full max-w-sm mx-4 p-6">
                        <h3 className="font-bold text-lg text-gray-900 dark:text-white mb-4">Add Members</h3>
                        <p className="text-xs text-gray-500 dark:text-gray-400 mb-2">Enter user IDs (comma-separated)</p>
                        <input
                            autoFocus
                            value={addMemberIds}
                            onChange={e => setAddMemberIds(e.target.value)}
                            placeholder="e.g. 2, 3, 5"
                            className="w-full px-4 py-2 border border-gray-200 dark:border-gray-600 rounded-xl bg-gray-50 dark:bg-gray-700 text-sm text-gray-900 dark:text-gray-100 outline-none focus:border-teal-500 mb-4"
                        />
                        <div className="flex gap-2">
                            <button
                                onClick={() => { setShowAddMember(false); setAddMemberIds(""); }}
                                className="flex-1 py-2 rounded-xl border border-gray-200 dark:border-gray-600 text-sm text-gray-700 dark:text-gray-300"
                            >Cancel</button>
                            <button
                                onClick={handleAddMembers}
                                disabled={!addMemberIds.trim()}
                                className="flex-1 py-2 bg-teal-600 text-white rounded-xl text-sm font-semibold disabled:opacity-50 hover:bg-teal-700 transition"
                            >Add</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
