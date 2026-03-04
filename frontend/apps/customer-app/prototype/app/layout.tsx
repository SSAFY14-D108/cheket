import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import Link from "next/link";
import {
  House,
  Music2,
  Search,
  Star,
  Tag,
  Ticket,
  UserCircle,
} from "lucide-react";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "CHEKET",
  description: "CHEKET customer web prototype",
};

const bottomNavItems = [
  { href: "/", label: "\uD648", icon: House },
  { href: "/concerts", label: "\uACF5\uC5F0", icon: Music2 },
  { href: "/resale", label: "\uC591\uB3C4", icon: Tag },
  { href: "/my-tickets", label: "\uD2F0\uCF13", icon: Ticket },
  { href: "/collection", label: "\uBCF4\uAD00\uD568", icon: Star },
];

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        <div className="mx-auto flex min-h-screen w-full max-w-[430px] flex-col border-x border-border bg-background shadow-[0_0_0_1px_rgba(0,0,0,0.02)]">
          <header className="sticky top-0 z-20 border-b border-border bg-background/95 px-4 py-3 backdrop-blur">
            <div className="flex items-center justify-between">
              <strong className="text-base font-semibold tracking-tight text-primary">
                CHEKET
              </strong>
              <div className="flex items-center gap-1">
                <Link
                  href="/concerts"
                  className="flex h-8 w-8 items-center justify-center rounded-full border border-border bg-card text-muted-foreground"
                  aria-label="\uACF5\uC5F0"
                >
                  <Search size={16} />
                </Link>
                <Link
                  href="/my"
                  className="flex h-8 w-8 items-center justify-center rounded-full border border-border bg-card text-muted-foreground"
                  aria-label="\uB9C8\uC774\uD398\uC774\uC9C0"
                >
                  <UserCircle size={16} />
                </Link>
              </div>
            </div>
          </header>

          <main className="flex-1 px-4 py-4 pb-24">{children}</main>

          <nav className="fixed bottom-0 z-30 w-full max-w-[430px] border-t border-border bg-background/95 backdrop-blur">
            <ul className="grid grid-cols-5">
              {bottomNavItems.map((item) => {
                const Icon = item.icon;

                return (
                  <li key={item.href}>
                    <Link
                      href={item.href}
                      className="flex h-16 flex-col items-center justify-center gap-1 text-[11px] text-muted-foreground transition-colors hover:text-foreground"
                    >
                      <Icon size={18} />
                      <span>{item.label}</span>
                    </Link>
                  </li>
                );
              })}
            </ul>
          </nav>
        </div>
      </body>
    </html>
  );
}
