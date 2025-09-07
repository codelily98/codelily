"use client";

import { useState } from "react";
import dynamic from "next/dynamic";
import api from "@/utils/api";

const MarkdownEditor = dynamic(() => import("@uiw/react-md-editor"), {
    ssr: false,
});

export default function NewPostPage() {
    const [title, setTitle] = useState("");
    const [content, setContent] = useState<string | undefined>("");

    const handleSubmit = async () => {
        await api.post("/posts", { title, content });
        window.location.href = "/blog";
    };

    return (
        <div className="bg-white p-6 rounded-lg shadow-md">
            <h1 className="text-2xl font-bold mb-4">새 글 작성</h1>
            <input
                type="text"
                placeholder="제목을 입력하세요"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className="w-full p-2 border rounded mb-4"
            />
            <MarkdownEditor
                value={content}
                onChange={setContent}
                height={400}
            />
            <button
                onClick={handleSubmit}
                className="mt-4 bg-primary text-white px-4 py-2 rounded hover:bg-green-500"
            >
                작성 완료
            </button>
        </div>
    );
}
