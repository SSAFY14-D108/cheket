import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import Link from "next/link";
import {
  CircleUserRound,
  House,
  Search,
  Ticket,
  WalletCards,
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
  description: "Cheket customer web prototype",
};

const bottomNavItems = [
  { href: "/", label: "Home", icon: House },
  { href: "/search", label: "Search", icon: Search },
  { href: "/tickets", label: "Tickets", icon: Ticket },
  { href: "/wallet", label: "Wallet", icon: WalletCards },
  { href: "/my", label: "My", icon: CircleUserRound },
];

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <div className="mx-auto flex min-h-screen w-full max-w-[430px] flex-col border-x border-border bg-background shadow-[0_0_0_1px_rgba(0,0,0,0.02)]">
          <header className="sticky top-0 z-20 border-b border-border bg-background/95 px-4 py-3 backdrop-blur">
            <div className="flex items-center justify-between">
              <strong className="text-base font-semibold tracking-tight text-primary">
                CHEKET
              </strong>
              <button
                type="button"
                className="rounded-md border border-border bg-card px-2.5 py-1 text-sm text-foreground"
              >
                Sign in
              </button>
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
