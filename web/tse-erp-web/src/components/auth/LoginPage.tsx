// src/pages/auth/LoginPage.tsx

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Eye, EyeOff, BarChart3, Lock, User } from 'lucide-react'
import { useAuth } from '../../hooks/useAuth'

interface LoginForm {
  username: string
  password: string
}

export default function LoginPage() {
  const [showPassword, setShowPassword] = useState(false)
  const { loginMutation } = useAuth()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginForm>({
    defaultValues: { username: '', password: '' },
  })

  const onSubmit = (data: LoginForm) => {
    loginMutation.mutate(data)
  }

  const errorMessage = loginMutation.error instanceof Error
    ? loginMutation.error.message
    : loginMutation.isError
    ? 'Login failed. Please try again.'
    : null

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-blue-950 to-slate-900 flex items-center justify-center p-4">

      {/* Background pattern */}
      <div className="absolute inset-0 opacity-10"
        style={{
          backgroundImage: `radial-gradient(circle at 25px 25px, white 2px, transparent 0)`,
          backgroundSize: '50px 50px'
        }}
      />

      <div className="relative w-full max-w-md">

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-2xl overflow-hidden">

          {/* Header */}
          <div className="bg-gradient-to-r from-blue-600 to-blue-500 px-8 py-8 text-center">
            <div className="flex items-center justify-center gap-3 mb-2">
              <div className="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center">
                <BarChart3 size={20} className="text-white" />
              </div>
              <h1 className="text-2xl font-bold text-white">TSE-ERP</h1>
            </div>
            <p className="text-blue-100 text-sm">Enterprise Resource Planning</p>
          </div>

          {/* Form */}
          <div className="px-8 py-8">
            <div className="mb-6">
              <h2 className="text-lg font-semibold text-gray-800">Welcome back</h2>
              <p className="text-sm text-gray-500 mt-0.5">Sign in to your account</p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">

              {/* Username */}
              <div>
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                  Username
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-3 flex items-center pointer-events-none">
                    <User size={15} className="text-gray-400" />
                  </div>
                  <input
                    {...register('username', { required: 'Username is required' })}
                    placeholder="Enter username"
                    autoComplete="username"
                    className={`w-full border rounded-lg pl-9 pr-3 py-2.5 text-sm outline-none transition-colors
                      ${errors.username
                        ? 'border-red-300 bg-red-50 focus:border-red-400'
                        : 'border-gray-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'
                      }`}
                  />
                </div>
                {errors.username && (
                  <p className="text-xs text-red-500 mt-1">{errors.username.message}</p>
                )}
              </div>

              {/* Password */}
              <div>
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                  Password
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-3 flex items-center pointer-events-none">
                    <Lock size={15} className="text-gray-400" />
                  </div>
                  <input
                    {...register('password', { required: 'Password is required' })}
                    type={showPassword ? 'text' : 'password'}
                    placeholder="Enter password"
                    autoComplete="current-password"
                    className={`w-full border rounded-lg pl-9 pr-10 py-2.5 text-sm outline-none transition-colors
                      ${errors.password
                        ? 'border-red-300 bg-red-50 focus:border-red-400'
                        : 'border-gray-200 focus:border-blue-400 focus:ring-2 focus:ring-blue-50'
                      }`}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-3 flex items-center text-gray-400 hover:text-gray-600 transition-colors"
                  >
                    {showPassword ? <EyeOff size={15} /> : <Eye size={15} />}
                  </button>
                </div>
                {errors.password && (
                  <p className="text-xs text-red-500 mt-1">{errors.password.message}</p>
                )}
              </div>

              {/* Error message from backend */}
              {errorMessage && (
                <div className="flex items-start gap-2.5 bg-red-50 border border-red-200 rounded-lg px-3.5 py-3">
                  <div className="w-4 h-4 rounded-full bg-red-500 flex items-center justify-center flex-shrink-0 mt-0.5">
                    <span className="text-white text-xs font-bold leading-none">!</span>
                  </div>
                  <p className="text-sm text-red-700">{errorMessage}</p>
                </div>
              )}

              {/* Submit */}
              <button
                type="submit"
                disabled={loginMutation.isPending}
                className="w-full py-2.5 bg-blue-600 hover:bg-blue-700 active:bg-blue-800 disabled:opacity-60 disabled:cursor-not-allowed text-white text-sm font-semibold rounded-lg transition-colors flex items-center justify-center gap-2 mt-2"
              >
                {loginMutation.isPending ? (
                  <>
                    <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    Logging in...
                  </>
                ) : 'Login'}
              </button>

            </form>
          </div>

          {/* Footer */}
          <div className="px-8 py-4 bg-gray-50 border-t border-gray-100 text-center">
            <p className="text-xs text-gray-400">TSE-ERP v1.0 · Test Company Limited</p>
          </div>
        </div>

      </div>
    </div>
  )
}