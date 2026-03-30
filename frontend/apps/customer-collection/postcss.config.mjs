/** @type {import('postcss-load-config').Config} */
const config = {
  plugins: {
    '@tailwindcss/postcss': {},
    'postcss-preset-env': {
      stage: 2,
      features: {
        'nesting-rules': true,
        'custom-properties': false,
      },
      browsers: 'Chrome >= 80, Android >= 80',
    },
  },
}

export default config
