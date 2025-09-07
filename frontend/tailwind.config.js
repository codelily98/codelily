/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
        "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
        "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
    ],
    theme: {
        extend: {
            colors: {
                primary: "#c5f11f", // 연두색
                secondary: "#99e4fa", // 하늘색
                grayBg: "#f5f5f5", // 전체 wrap 배경
            },
            fontFamily: {
                sans: ["Pretendard", "Arial", "sans-serif"],
            },
        },
    },
    plugins: [],
};
