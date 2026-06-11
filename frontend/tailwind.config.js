import typography from '@tailwindcss/typography';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        surface: '#000000',
        surfaceDark: '#000000',
        surfaceLight: '#000000',
        surfaceGlass: 'rgba(0, 0, 0, 0.85)',
        border: 'rgba(255,255,255,0.08)',
        accent: '#FFFFFF',
        accentSoft: 'rgba(255,255,255,0.12)',
        accentGlow: '#FFFFFF',
        textPrimary: '#FFFFFF',
        textSecondary: 'rgba(255,255,255,0.65)',
      },
      boxShadow: {
        glow: '0 0 0 1px rgba(255,255,255,0.05)',
        glowSoft: '0 0 0 1px rgba(255,255,255,0.05), 0 10px 30px rgba(0, 0, 0, 0.35)',
      },
      backgroundImage: {
        'hero-radial': 'none',
      },
    },
  },
  plugins: [typography],
};
