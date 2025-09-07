"use client";
import { atom, selector } from "recoil";

export const authAtom = atom<{
    userId: number | null;
    role: "USER" | "ADMIN";
    nickname?: string;
    avatar?: string;
}>({
    key: "authAtom",
    default: { userId: null, role: "USER" },
});

export const isLoggedInSelector = selector({
    key: "isLoggedInSelector",
    get: ({ get }) => Boolean(get(authAtom).userId),
});
