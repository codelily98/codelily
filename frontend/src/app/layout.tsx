"use client";

import "./styles/globals.css";
import { RecoilRoot } from "recoil";
import Header from "./components/Header";
import Footer from "./components/Footer";

export default function RootLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return (
        <html lang="ko">
            <body>
                <RecoilRoot>
                    <div className="wrap bg-gray-50 min-h-screen flex flex-col">
                        <Header />
                        <main className="flex-1 container mx-auto p-6">
                            {children}
                        </main>
                        <Footer />
                    </div>
                </RecoilRoot>
            </body>
        </html>
    );
}
