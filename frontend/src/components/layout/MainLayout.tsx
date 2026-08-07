import { Outlet } from 'react-router-dom'

import { Footer } from './Footer'
import { Navbar } from './Navbar'

/**
 * Application shell rendered once around every routed page.
 *
 * Navbar and Footer are persistent; <Outlet /> swaps in the matched
 * page. flex-1 on the main element pins the footer to the bottom of
 * the viewport on short pages.
 */
export function MainLayout() {
  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />
      <main className="flex-1">
        <Outlet />
      </main>
      <Footer />
    </div>
  )
}