import { authHandlers, authTokenHandlers } from "./auth-handlers"
import { dashboardHandlers } from "./dashboard-handlers"
import { mypageHandlers } from "./mypage-handlers"
import { showHandlers } from "./show-handlers"

export const handlers = [
  ...authHandlers,
  ...mypageHandlers,
  ...dashboardHandlers,
  ...showHandlers,
  ...authTokenHandlers,
]
