import "./globals.css";
import Providers from "./providers";

export default function RootLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return (
        <html lang="ko" suppressHydrationWarning>
            <body className="min-h-dvh bg-slate-50 text-slate-900">
                <Providers>{children}</Providers>
            </body>
        </html>
    );
}
