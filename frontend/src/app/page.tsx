export default function Home() {
    return (
        <main className="mx-auto max-w-screen-xl px-4 py-10">
            <section className="grid gap-6 md:grid-cols-2 items-center">
                <div className="space-y-4">
                    <h1 className="text-3xl md:text-5xl font-extrabold tracking-tight">
                        Next Step: Codelily
                    </h1>
                    <p className="text-slate-600">
                        포트폴리오 + 블로그를 AWS 프리 티어로 운영하는 개발자
                        플랫폼
                    </p>
                    <div className="flex gap-3">
                        <a
                            href="/blog"
                            className="rounded-xl px-4 py-2 bg-emerald-600 text-white hover:bg-emerald-700"
                        >
                            블로그 보러가기
                        </a>
                        <a
                            href="/projects"
                            className="rounded-xl px-4 py-2 border border-slate-300 hover:bg-slate-100"
                        >
                            프로젝트
                        </a>
                    </div>
                </div>
                <div className="aspect-video rounded-2xl border border-slate-200 bg-gradient-to-br from-emerald-50 to-white" />
            </section>
        </main>
    );
}
