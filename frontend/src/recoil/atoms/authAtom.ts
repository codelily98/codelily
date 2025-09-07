import { atom } from "recoil";
import { User } from "@/types/user";

export const authState = atom<User | null>({
    key: "authState",
    default: null,
});
