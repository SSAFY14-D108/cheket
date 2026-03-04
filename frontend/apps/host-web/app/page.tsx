import { LoginForm } from "@/components/login/LoginForm"
import { LoginInput } from "@/components/common/LoginInput"
import { LoginButton } from "@/components/common/LoginButton"

export default function HomePage() {
  return (
    <main className="flex min-h-svh flex-col items-center justify-center gap-16 bg-background text-foreground py-20">

      {/* 1. LoginForm 전체를 재활용하는 예시 */}
      <section className="flex flex-col items-center gap-4 w-full">
        <h2 className="text-xl font-bold">1. 모듈화된 폼(Form) 전체를 두 번 재활용하기</h2>
        <p className="text-sm text-gray-500 mb-4">LoginForm 컴포넌트를 두 번 불렀을 뿐인데, 완벽하게 독립적인 폼 2개가 생깁니다.</p>
        <div className="flex w-full max-w-5xl justify-center gap-8 flex-col md:flex-row items-center">
          <LoginForm />
          <LoginForm />
        </div>
      </section>

      {/* 구분선 */}
      <div className="w-full max-w-5xl border-b border-gray-300"></div>

      {/* 2. LoginInput과 LoginButton 껍데기만 따로 빼서 전혀 다른 용도로 재활용하는 예시 */}
      <section className="flex flex-col items-center gap-4 w-full max-w-md">
        <h2 className="text-xl font-bold">2. 공통 부품(Input, Button)을 다른 용도로 재활용하기</h2>
        <p className="text-sm text-gray-500 text-center mb-4">
          '로그인' 이라는 이름이 붙어있지만,<br />사실은 그냥 '예쁜 입력창'과 '예쁜 버튼'일 뿐입니다.<br />이렇게 뉴스레터 구독 창으로도 순식간에 조립할 수 있습니다.
        </p>

        <div className="w-full flex flex-col gap-3 p-8 bg-zinc-100 rounded-xl overflow-hidden shadow-sm">
          <h3 className="font-semibold text-lg text-center">뉴스레터 구독하기</h3>
          <LoginInput
            type="email"
            placeholder="이메일을 입력하세요"
            className="bg-white border-blue-500 border-2"
          />
          <LoginInput
            type="text"
            placeholder="이름을 입력하세요"
            className="bg-white"
          />
          <LoginButton className="bg-blue-600 text-white hover:bg-blue-700">
            구독 완료!
          </LoginButton>
        </div>
      </section>

    </main>
  )
}

