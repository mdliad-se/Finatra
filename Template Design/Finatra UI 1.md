<!-- Financial Analytics -->
<!DOCTYPE html>

<html class="light" lang="en"><head>
<meta charset="utf-8"/>
<meta content="width=device-width, initial-scale=1.0" name="viewport"/>
<title>Finatra Analytics &amp; Reports</title>
<!-- Fonts -->
<link href="https://fonts.googleapis.com" rel="preconnect"/>
<link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect"/>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&amp;family=Poppins:wght@600;700&amp;display=swap" rel="stylesheet"/>
<!-- Material Symbols -->
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&amp;display=swap" rel="stylesheet"/>
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&amp;display=swap" rel="stylesheet"/>
<!-- Tailwind -->
<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<!-- Shared Tailwind Config -->
<script id="tailwind-config">
      tailwind.config = {
        darkMode: "class",
        theme: {
          extend: {
            "colors": {
                    "inverse-primary": "#7fd6ca",
                    "tertiary-fixed-dim": "#d7c4aa",
                    "secondary-fixed-dim": "#bacac8",
                    "on-secondary-fixed": "#101e1d",
                    "error-container": "#ffdad6",
                    "surface-dim": "#dfd9d3",
                    "on-surface": "#1e1b18",
                    "dark-teal-surface": "#1A3330",
                    "outline-variant": "#bdc9c6",
                    "primary-fixed": "#9bf2e6",
                    "secondary-container": "#d6e6e4",
                    "on-error-container": "#93000a",
                    "on-tertiary-fixed": "#241a09",
                    "on-primary-fixed-variant": "#00504a",
                    "outline": "#6e7977",
                    "system-red": "#B00020",
                    "secondary-fixed": "#d6e6e4",
                    "primary": "#005b53",
                    "on-background": "#1e1b18",
                    "on-tertiary": "#ffffff",
                    "on-secondary-container": "#586766",
                    "inverse-on-surface": "#f6f0ea",
                    "on-primary-container": "#a1f8ec",
                    "background": "#fff8f2",
                    "error": "#ba1a1a",
                    "on-error": "#ffffff",
                    "tertiary-fixed": "#f4dfc5",
                    "surface-container": "#f3ede7",
                    "ink-text": "#1A1A1A",
                    "on-surface-variant": "#3e4947",
                    "on-secondary": "#ffffff",
                    "on-tertiary-container": "#fae5ca",
                    "primary-fixed-dim": "#7fd6ca",
                    "surface": "#fff8f2",
                    "surface-container-low": "#f9f2ec",
                    "on-primary": "#ffffff",
                    "deep-ink-bg": "#0F1F1E",
                    "tertiary-container": "#756651",
                    "surface-bright": "#fff8f2",
                    "primary-container": "#0a756c",
                    "surface-container-highest": "#e8e1dc",
                    "surface-container-high": "#eee7e1",
                    "on-tertiary-fixed-variant": "#524531",
                    "inverse-surface": "#33302c",
                    "tertiary": "#5c4e3a",
                    "on-primary-fixed": "#00201d",
                    "surface-tint": "#006a62",
                    "secondary": "#526160",
                    "on-secondary-fixed-variant": "#3b4a48",
                    "surface-variant": "#e8e1dc",
                    "surface-container-lowest": "#ffffff"
            },
            "borderRadius": {
                    "DEFAULT": "0.25rem",
                    "lg": "0.5rem",
                    "xl": "0.75rem",
                    "full": "9999px"
            },
            "spacing": {
                    "margin-tablet": "24px",
                    "stack-sm": "8px",
                    "stack-md": "16px",
                    "gutter": "16px",
                    "stack-lg": "24px",
                    "margin-mobile": "16px",
                    "margin-desktop": "32px"
            },
            "fontFamily": {
                    "body-md": ["Inter"],
                    "headline-md": ["Poppins"],
                    "headline-lg": ["Poppins"],
                    "title-md": ["Inter"],
                    "headline-lg-mobile": ["Poppins"],
                    "body-lg": ["Inter"],
                    "label-md": ["Inter"]
            },
            "fontSize": {
                    "body-md": ["14px", {"lineHeight": "20px", "fontWeight": "400"}],
                    "headline-md": ["28px", {"lineHeight": "36px", "fontWeight": "600"}],
                    "headline-lg": ["48px", {"lineHeight": "56px", "letterSpacing": "-0.02em", "fontWeight": "700"}],
                    "title-md": ["18px", {"lineHeight": "24px", "fontWeight": "600"}],
                    "headline-lg-mobile": ["32px", {"lineHeight": "40px", "fontWeight": "700"}],
                    "body-lg": ["16px", {"lineHeight": "24px", "fontWeight": "400"}],
                    "label-md": ["12px", {"lineHeight": "16px", "letterSpacing": "0.5px", "fontWeight": "500"}]
            }
          },
        },
      }
    </script>
<style>
        .material-symbols-outlined {
            font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
        }
        .chart-container {
            position: relative;
            width: 100%;
        }
        .glass-card {
            backdrop-filter: blur(15px);
            background: rgba(214, 230, 228, 0.4);
        }
        ::-webkit-scrollbar {
            width: 8px;
        }
        ::-webkit-scrollbar-track {
            background: transparent;
        }
        ::-webkit-scrollbar-thumb {
            background: #bdc9c6;
            border-radius: 10px;
        }
    </style>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
  </head>
<body class="bg-background text-on-background font-body-md min-h-screen pb-24 md:pb-0">
<!-- Top App Bar -->
<header class="sticky top-0 z-40 bg-surface/80 backdrop-blur-md w-full flex items-center justify-between px-margin-mobile py-stack-sm md:px-margin-desktop">
<div class="flex items-center gap-3">
<span class="material-symbols-outlined text-primary" style="font-size: 28px;">account_balance_wallet</span>
<h1 class="text-headline-md font-headline-md text-primary tracking-tight">Finatra</h1>
</div>
<div class="flex items-center gap-4">
<button class="w-10 h-10 rounded-full bg-secondary-container/50 flex items-center justify-center hover:bg-secondary-container transition-colors active:scale-95">
<span class="material-symbols-outlined text-on-surface-variant">search</span>
</button>
<div class="w-10 h-10 rounded-full bg-primary-fixed flex items-center justify-center overflow-hidden border border-outline-variant/30">
<img alt="User Profile" class="w-full h-full object-cover" data-alt="A high-quality professional portrait of a person in a minimalist, bright setting. The person has a friendly, intelligent expression, reflecting the privacy-centric and grounded personality of the Finatra brand. The background is a soft Mist Teal, maintaining the Material 3 Expressive aesthetic with clean lines and premium lighting." src="https://lh3.googleusercontent.com/aida-public/AB6AXuCS41_WgTybfTl1xgNTmV0e9xdyumro9OIohBRgEQJ_pZORoMZwSFbXwrlqyR5Mvsl2VVbK7AHUafSkhzCGv-w7pkaKmkd7JqRpYgAUbIdH6LK_r4TrEJztQsGHzIZo0Bd7SHJdL-HE8xOUpBZPZrlyF7XqRQ3euFlYyK85vBxNX1vDqI8dmPD5IEK3S0IjaXDUfGAJBvgm3Sa5462GIzygNocByFhMNt_-BzM6TrZrwNK9bjiA22j4wvlHWVWe9Oucc4mlAZSCvK9H"/>
</div>
</div>
</header>
<!-- Main Content -->
<main class="max-w-7xl mx-auto px-margin-mobile py-stack-lg md:px-margin-desktop md:py-stack-lg">
<!-- Dashboard Header -->
<div class="mb-stack-lg">
<h2 class="text-headline-lg-mobile md:text-headline-lg font-headline-lg text-primary mb-2">Analytics</h2>
<p class="text-body-lg text-on-surface-variant">Deep dive into your financial health and spending patterns.</p>
</div>
<!-- Bento Grid Layout -->
<div class="grid grid-cols-1 md:grid-cols-12 gap-gutter">
<!-- Net Worth Summary Card (Level 1 Surface) -->
<div class="md:col-span-4 bg-secondary-container/30 rounded-xl p-stack-lg border border-outline-variant/30 transition-transform hover:scale-[1.01] active:scale-[0.99] cursor-default">
<div class="flex justify-between items-start mb-4">
<span class="text-label-md font-label-md text-on-surface-variant uppercase tracking-widest">Net Worth</span>
<span class="material-symbols-outlined text-primary">trending_up</span>
</div>
<div class="text-headline-md font-headline-md text-on-surface mb-1">$142,580.00</div>
<div class="flex items-center gap-2 text-primary font-title-md">
<span class="material-symbols-outlined" style="font-size: 18px;">arrow_upward</span>
<span>+4.2% this month</span>
</div>
<!-- Small sparkline-style graph -->
<div class="mt-6 h-24 w-full opacity-60">
<svg class="w-full h-full" viewbox="0 0 400 100">
<path class="text-primary" d="M0,80 Q50,70 100,75 T200,40 T300,50 T400,20" fill="none" stroke="currentColor" stroke-width="3"></path>
</svg>
</div>
</div>
<!-- Spending Trends Line Chart (Main focus) -->
<div class="md:col-span-8 bg-surface-container-low rounded-xl p-stack-lg border border-outline-variant/20 flex flex-col">
<div class="flex justify-between items-center mb-6">
<h3 class="text-title-md font-title-md text-primary">Spending Trends</h3>
<div class="flex bg-surface-container rounded-full p-1">
<button class="px-4 py-1 text-label-md rounded-full bg-primary text-on-primary transition-all">Week</button>
<button class="px-4 py-1 text-label-md rounded-full text-on-surface-variant hover:bg-secondary-container/50">Month</button>
</div>
</div>
<div class="flex-grow flex items-end gap-2 min-h-[220px] pb-4">
<!-- Manual SVG Chart for Expressive Look -->
<div class="relative w-full h-full">
<!-- Grid lines -->
<div class="absolute inset-0 flex flex-col justify-between opacity-10">
<div class="border-t border-on-surface-variant w-full"></div>
<div class="border-t border-on-surface-variant w-full"></div>
<div class="border-t border-on-surface-variant w-full"></div>
<div class="border-t border-on-surface-variant w-full"></div>
</div>
<!-- Area Chart SVG -->
<svg class="w-full h-full preserve-3d" viewbox="0 0 800 200">
<defs>
<lineargradient id="chartGradient" x1="0" x2="0" y1="0" y2="1">
<stop offset="0%" stop-color="var(--primary)" stop-opacity="0.3"></stop>
<stop offset="100%" stop-color="var(--primary)" stop-opacity="0"></stop>
</lineargradient>
</defs>
<path d="M0,180 C100,160 150,190 200,140 S300,100 400,120 S550,40 700,60 S800,20 800,20 V200 H0 Z" fill="url(#chartGradient)"></path>
<path d="M0,180 C100,160 150,190 200,140 S300,100 400,120 S550,40 700,60 S800,20 800,20" fill="none" stroke="#005b53" stroke-linecap="round" stroke-width="4"></path>
<!-- Active Point -->
<circle cx="550" cy="40" fill="#005b53" r="6"></circle>
<circle cx="550" cy="40" fill="#005b53" fill-opacity="0.2" r="12"></circle>
</svg>
<div class="absolute top-2 left-[550px] transform -translate-x-1/2 -translate-y-full glass-card border border-outline-variant/30 rounded-lg px-3 py-1.5 text-label-md shadow-lg">
                            $1,240.50
                        </div>
</div>
</div>
<div class="flex justify-between text-label-md text-on-surface-variant pt-2 border-t border-outline-variant/10">
<span>Mon</span><span>Tue</span><span>Wed</span><span>Thu</span><span>Fri</span><span>Sat</span><span>Sun</span>
</div>
</div>
<!-- Income vs Expense Monthly Bar Chart -->
<div class="md:col-span-7 bg-surface-container-low rounded-xl p-stack-lg border border-outline-variant/20">
<div class="flex justify-between items-center mb-6">
<h3 class="text-title-md font-title-md text-primary">Monthly Comparison</h3>
<span class="text-label-md text-on-surface-variant">Last 6 Months</span>
</div>
<div class="h-64 flex items-end justify-between px-2 gap-4">
<!-- Custom Bar Component -->
<div class="flex-1 flex flex-col items-center gap-1 group">
<div class="w-full flex gap-1 items-end h-48">
<div class="flex-1 bg-primary rounded-t-lg transition-all h-[60%] group-hover:h-[65%]"></div>
<div class="flex-1 bg-tertiary-container rounded-t-lg transition-all h-[40%] group-hover:h-[45%]"></div>
</div>
<span class="text-label-md text-on-surface-variant">Jan</span>
</div>
<div class="flex-1 flex flex-col items-center gap-1 group">
<div class="w-full flex gap-1 items-end h-48">
<div class="flex-1 bg-primary rounded-t-lg h-[80%]"></div>
<div class="flex-1 bg-tertiary-container rounded-t-lg h-[50%]"></div>
</div>
<span class="text-label-md text-on-surface-variant">Feb</span>
</div>
<div class="flex-1 flex flex-col items-center gap-1 group">
<div class="w-full flex gap-1 items-end h-48">
<div class="flex-1 bg-primary rounded-t-lg h-[70%]"></div>
<div class="flex-1 bg-tertiary-container rounded-t-lg h-[65%]"></div>
</div>
<span class="text-label-md text-on-surface-variant">Mar</span>
</div>
<div class="flex-1 flex flex-col items-center gap-1 group">
<div class="w-full flex gap-1 items-end h-48">
<div class="flex-1 bg-primary rounded-t-lg h-[90%]"></div>
<div class="flex-1 bg-tertiary-container rounded-t-lg h-[30%]"></div>
</div>
<span class="text-label-md text-on-surface-variant">Apr</span>
</div>
<div class="flex-1 flex flex-col items-center gap-1 group">
<div class="w-full flex gap-1 items-end h-48">
<div class="flex-1 bg-primary rounded-t-lg h-[85%]"></div>
<div class="flex-1 bg-tertiary-container rounded-t-lg h-[40%]"></div>
</div>
<span class="text-label-md text-on-surface-variant">May</span>
</div>
<div class="flex-1 flex flex-col items-center gap-1 group">
<div class="w-full flex gap-1 items-end h-48">
<div class="flex-1 bg-primary rounded-t-lg h-[75%]"></div>
<div class="flex-1 bg-tertiary-container rounded-t-lg h-[55%]"></div>
</div>
<span class="text-label-md text-on-surface-variant">Jun</span>
</div>
</div>
<div class="mt-6 flex gap-6 justify-center">
<div class="flex items-center gap-2">
<div class="w-3 h-3 rounded-full bg-primary"></div>
<span class="text-label-md">Income</span>
</div>
<div class="flex items-center gap-2">
<div class="w-3 h-3 rounded-full bg-tertiary-container"></div>
<span class="text-label-md">Expenses</span>
</div>
</div>
</div>
<!-- Category Breakdown Donut Chart Card -->
<div class="md:col-span-5 bg-surface-container-low rounded-xl p-stack-lg border border-outline-variant/20 flex flex-col items-center">
<h3 class="text-title-md font-title-md text-primary w-full mb-6">Top Categories</h3>
<div class="relative w-48 h-48 mb-6">
<!-- SVG Donut Chart -->
<svg class="w-full h-full transform -rotate-90" viewbox="0 0 36 36">
<circle cx="18" cy="18" fill="transparent" r="15.915" stroke="#e8e1dc" stroke-width="4"></circle>
<circle cx="18" cy="18" fill="transparent" r="15.915" stroke="#005b53" stroke-dasharray="45 55" stroke-dashoffset="0" stroke-width="4"></circle>
<circle cx="18" cy="18" fill="transparent" r="15.915" stroke="#5c4e3a" stroke-dasharray="25 75" stroke-dashoffset="-45" stroke-width="4"></circle>
<circle cx="18" cy="18" fill="transparent" r="15.915" stroke="#526160" stroke-dasharray="30 70" stroke-dashoffset="-70" stroke-width="4"></circle>
</svg>
<div class="absolute inset-0 flex flex-col items-center justify-center">
<span class="text-label-md text-on-surface-variant">Total</span>
<span class="text-title-md font-title-md">$4,280</span>
</div>
</div>
<div class="w-full space-y-3">
<div class="flex justify-between items-center text-body-md">
<div class="flex items-center gap-3">
<div class="w-2 h-2 rounded-full bg-primary"></div>
<span>Housing</span>
</div>
<span class="font-bold text-on-surface">45%</span>
</div>
<div class="flex justify-between items-center text-body-md">
<div class="flex items-center gap-3">
<div class="w-2 h-2 rounded-full bg-tertiary"></div>
<span>Dining</span>
</div>
<span class="font-bold text-on-surface">25%</span>
</div>
<div class="flex justify-between items-center text-body-md">
<div class="flex items-center gap-3">
<div class="w-2 h-2 rounded-full bg-secondary"></div>
<span>Transport</span>
</div>
<span class="font-bold text-on-surface">30%</span>
</div>
</div>
</div>
<!-- Insights / Quick Reports -->
<div class="md:col-span-12 grid grid-cols-1 md:grid-cols-3 gap-gutter">
<div class="bg-surface-container rounded-xl p-stack-md flex gap-4 items-center">
<div class="w-12 h-12 rounded-full bg-primary-container/20 flex items-center justify-center text-primary">
<span class="material-symbols-outlined">lightbulb</span>
</div>
<div>
<div class="text-label-md font-label-md text-on-surface-variant">Spending Alert</div>
<div class="text-body-md font-title-md">Dining out is 12% higher than last month.</div>
</div>
</div>
<div class="bg-surface-container rounded-xl p-stack-md flex gap-4 items-center">
<div class="w-12 h-12 rounded-full bg-tertiary-container/20 flex items-center justify-center text-tertiary">
<span class="material-symbols-outlined">savings</span>
</div>
<div>
<div class="text-label-md font-label-md text-on-surface-variant">Savings Goal</div>
<div class="text-body-md font-title-md">You're $250 away from your Vacay goal!</div>
</div>
</div>
<div class="bg-surface-container rounded-xl p-stack-md flex gap-4 items-center">
<div class="w-12 h-12 rounded-full bg-secondary-container/30 flex items-center justify-center text-secondary">
<span class="material-symbols-outlined">history_edu</span>
</div>
<div>
<div class="text-label-md font-label-md text-on-surface-variant">Auto-Categorized</div>
<div class="text-body-md font-title-md">98% of transactions parsed via AI.</div>
</div>
</div>
</div>
</div>
</main>
<!-- Bottom Navigation Bar (Mobile) -->
<nav class="md:hidden fixed bottom-0 left-0 w-full h-20 bg-surface/80 backdrop-blur-md border-t border-outline-variant/30 flex justify-around items-center px-2 pb-safe z-50">
<a class="flex flex-col items-center justify-center text-on-secondary-container/70 px-5 py-1 hover:bg-surface-container-high transition-colors active:scale-90" href="#">
<span class="material-symbols-outlined">home</span>
<span class="text-label-md font-label-md">Home</span>
</a>
<a class="flex flex-col items-center justify-center text-on-secondary-container/70 px-5 py-1 hover:bg-surface-container-high transition-colors active:scale-90" href="#">
<span class="material-symbols-outlined">receipt_long</span>
<span class="text-label-md font-label-md">Transactions</span>
</a>
<a class="flex flex-col items-center justify-center text-on-secondary-container/70 px-5 py-1 hover:bg-surface-container-high transition-colors active:scale-90" href="#">
<span class="material-symbols-outlined">account_balance</span>
<span class="text-label-md font-label-md">Budgets</span>
</a>
<a class="flex flex-col items-center justify-center bg-secondary-container text-on-secondary-container rounded-full px-5 py-1 active:scale-90 transition-all duration-200" href="#">
<span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">analytics</span>
<span class="text-label-md font-label-md">Analytics</span>
</a>
<a class="flex flex-col items-center justify-center text-on-secondary-container/70 px-5 py-1 hover:bg-surface-container-high transition-colors active:scale-90" href="#">
<span class="material-symbols-outlined">settings</span>
<span class="text-label-md font-label-md">Settings</span>
</a>
</nav>
<!-- Floating Action Button (FAB) - For New Report/Entry -->
<button class="fixed bottom-24 right-margin-mobile md:bottom-8 md:right-8 w-14 h-14 bg-primary text-on-primary rounded-2xl shadow-xl flex items-center justify-center hover:scale-110 active:scale-95 transition-all z-50 group">
<span class="material-symbols-outlined text-2xl transition-transform group-hover:rotate-90">add</span>
</button>
<!-- Side Navigation Shell (Desktop) -->
<div class="hidden md:flex fixed left-0 top-1/2 -translate-y-1/2 flex-col gap-6 p-4 z-40">
<div class="bg-surface-container rounded-2xl p-2 border border-outline-variant/30 shadow-sm flex flex-col gap-2">
<button class="p-3 rounded-xl hover:bg-secondary-container transition-colors group relative">
<span class="material-symbols-outlined text-on-surface-variant group-hover:text-primary">home</span>
<span class="absolute left-full ml-4 px-2 py-1 bg-inverse-surface text-inverse-on-surface rounded text-xs opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity">Home</span>
</button>
<button class="p-3 rounded-xl bg-primary text-on-primary shadow-lg transition-colors group relative">
<span class="material-symbols-outlined">analytics</span>
<span class="absolute left-full ml-4 px-2 py-1 bg-inverse-surface text-inverse-on-surface rounded text-xs opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity">Analytics</span>
</button>
<button class="p-3 rounded-xl hover:bg-secondary-container transition-colors group relative">
<span class="material-symbols-outlined text-on-surface-variant group-hover:text-primary">receipt_long</span>
</button>
<button class="p-3 rounded-xl hover:bg-secondary-container transition-colors group relative">
<span class="material-symbols-outlined text-on-surface-variant group-hover:text-primary">settings</span>
</button>
</div>
</div>
<script>
        // Micro-interaction for hover states on charts
        document.querySelectorAll('.flex-1.group').forEach(bar => {
            bar.addEventListener('mouseenter', () => {
                // Potential tooltip logic here
            });
        });

        // Simulating some dynamic value updates for extra "premium" feel
        const animatedNumbers = document.querySelectorAll('.font-headline-md');
        animatedNumbers.forEach(num => {
            if(num.textContent.includes('$')) {
                const finalValue = parseFloat(num.textContent.replace(/[$,]/g, ''));
                let current = 0;
                const duration = 1500;
                const start = performance.now();

                function step(timestamp) {
                    const progress = Math.min((timestamp - start) / duration, 1);
                    const value = progress * finalValue;
                    num.textContent = '$' + value.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2});
                    if (progress < 1) {
                        window.requestAnimationFrame(step);
                    }
                }
                window.requestAnimationFrame(step);
            }
        });
    </script>
</body></html>

<!-- Home Dashboard -->
<!DOCTYPE html>

<html class="light" lang="en"><head>
<meta charset="utf-8"/>
<meta content="width=device-width, initial-scale=1.0" name="viewport"/>
<title>Finatra | Home Dashboard</title>
<!-- Material Symbols -->
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&amp;display=swap" rel="stylesheet"/>
<!-- Google Fonts -->
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&amp;family=Poppins:wght@600;700&amp;family=Public+Sans:wght@400;600;700&amp;display=swap" rel="stylesheet"/>
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&amp;display=swap" rel="stylesheet"/>
<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<script id="tailwind-config">
        tailwind.config = {
            darkMode: "class",
            theme: {
                extend: {
                    "colors": {
                        "inverse-primary": "#7fd6ca",
                        "tertiary-fixed-dim": "#d7c4aa",
                        "secondary-fixed-dim": "#bacac8",
                        "on-secondary-fixed": "#101e1d",
                        "error-container": "#ffdad6",
                        "surface-dim": "#dfd9d3",
                        "on-surface": "#1e1b18",
                        "dark-teal-surface": "#1A3330",
                        "outline-variant": "#bdc9c6",
                        "primary-fixed": "#9bf2e6",
                        "secondary-container": "#d6e6e4",
                        "on-error-container": "#93000a",
                        "on-tertiary-fixed": "#241a09",
                        "on-primary-fixed-variant": "#00504a",
                        "outline": "#6e7977",
                        "system-red": "#B00020",
                        "secondary-fixed": "#d6e6e4",
                        "primary": "#005b53",
                        "on-background": "#1e1b18",
                        "on-tertiary": "#ffffff",
                        "on-secondary-container": "#586766",
                        "inverse-on-surface": "#f6f0ea",
                        "on-primary-container": "#a1f8ec",
                        "background": "#fff8f2",
                        "error": "#ba1a1a",
                        "on-error": "#ffffff",
                        "tertiary-fixed": "#f4dfc5",
                        "surface-container": "#f3ede7",
                        "ink-text": "#1A1A1A",
                        "on-surface-variant": "#3e4947",
                        "on-secondary": "#ffffff",
                        "on-tertiary-container": "#fae5ca",
                        "primary-fixed-dim": "#7fd6ca",
                        "surface": "#fff8f2",
                        "surface-container-low": "#f9f2ec",
                        "on-primary": "#ffffff",
                        "deep-ink-bg": "#0F1F1E",
                        "tertiary-container": "#756651",
                        "surface-bright": "#fff8f2",
                        "primary-container": "#0a756c",
                        "surface-container-highest": "#e8e1dc",
                        "surface-container-high": "#eee7e1",
                        "on-tertiary-fixed-variant": "#524531",
                        "inverse-surface": "#33302c",
                        "tertiary": "#5c4e3a",
                        "on-primary-fixed": "#00201d",
                        "surface-tint": "#006a62",
                        "secondary": "#526160",
                        "on-secondary-fixed-variant": "#3b4a48",
                        "surface-variant": "#e8e1dc",
                        "surface-container-lowest": "#ffffff"
                    },
                    "borderRadius": {
                        "DEFAULT": "0.25rem",
                        "lg": "0.5rem",
                        "xl": "0.75rem",
                        "full": "9999px"
                    },
                    "spacing": {
                        "margin-tablet": "24px",
                        "stack-sm": "8px",
                        "stack-md": "16px",
                        "gutter": "16px",
                        "stack-lg": "24px",
                        "margin-mobile": "16px",
                        "margin-desktop": "32px"
                    },
                    "fontFamily": {
                        "body-md": ["Inter"],
                        "headline-md": ["Poppins"],
                        "headline-lg": ["Poppins"],
                        "title-md": ["Inter"],
                        "headline-lg-mobile": ["Poppins"],
                        "body-lg": ["Inter"],
                        "label-md": ["Inter"]
                    },
                    "fontSize": {
                        "body-md": ["14px", {"lineHeight": "20px", "fontWeight": "400"}],
                        "headline-md": ["28px", {"lineHeight": "36px", "fontWeight": "600"}],
                        "headline-lg": ["48px", {"lineHeight": "56px", "letterSpacing": "-0.02em", "fontWeight": "700"}],
                        "title-md": ["18px", {"lineHeight": "24px", "fontWeight": "600"}],
                        "headline-lg-mobile": ["32px", {"lineHeight": "40px", "fontWeight": "700"}],
                        "body-lg": ["16px", {"lineHeight": "24px", "fontWeight": "400"}],
                        "label-md": ["12px", {"lineHeight": "16px", "letterSpacing": "0.5px", "fontWeight": "500"}]
                    }
                },
            },
        }
    </script>
<style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #fff8f2; /* Warm White */
        }
        .material-symbols-outlined {
            font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
        }
        .filled-icon {
            font-variation-settings: 'FILL' 1;
        }
        .glass-nav {
            backdrop-filter: blur(15px);
            -webkit-backdrop-filter: blur(15px);
        }
        /* Custom scrollbar */
        ::-webkit-scrollbar {
            width: 4px;
        }
        ::-webkit-scrollbar-track {
            background: transparent;
        }
        ::-webkit-scrollbar-thumb {
            background: #bdc9c6;
            border-radius: 10px;
        }
    </style>
</head>
<body class="bg-background text-on-background min-h-screen pb-24 md:pb-0 md:pt-20 overflow-x-hidden">
<!-- TopAppBar -->
<header class="fixed top-0 left-0 w-full z-40 glass-nav bg-surface/80 dark:bg-surface-dim/80 flex items-center justify-between px-margin-mobile py-stack-sm w-full">
<div class="flex items-center gap-3">
<img alt="Finatra Logo" class="w-8 h-8 rounded-full" src="data:image/png;base64,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tMDk6wsAJje0AgECBAgQIECAAAECBAgQaFaghtUFADUgWoIAAQIECBAgQIAAAQIECDQpUMfaAoA6FK1BgAABAgQIECBAgAABAgSaE6hlZQFALYwWIUCAAAECBAgQIECAAAECTQnUs64AoB5HqxAgQIAAAQIECBAgQIAAgWYEalpVAFATpGUIECBAgAABAgQIECBAgEATAnWtKQCoS9I6BAgQIECAAAECBAgQIECgfoHaVhQA1EZpIQIECBAgQIAAAQIECBAgULdAfesJAOqztBIBAgQIECBAgAABAgQIEKhXoMbVBAA1YlqKAAECBAgQIECAAAECBAjUKVDnWgKAOjWtRYAAAQIECBAgQIAAAQIE6hOodSUBQK2cFiNAgAABAgQIECBAgAABAnUJ1LuOAKBeT6sRIECAAAECBAgQIECAAIF6BGpeRQBQM6jlCBAgQIAAAQIECBAgQIBAHQJ1ryEAqFvUegQIECBAgAABAgQIECBAYHKB2lcQANROakECBAgQIECAAAECBAgQIDCpQP3nCwDqN7UiAQIECBAgQIAAAQIECBCYTKCBswUADaBakgABAgQIECBAgAABAgQITCLQxLkCgCZUrUmAAAECBAgQIECAAAECBMYXaORMAUAjrBYlQIAAAQIECBAgQIAAAQLjCjRzngCgGVerEiBAgAABAgQIECBAgACB8QQaOksA0BCsZQkQIECAAAECBAgQIECAwDgCTZ0jAGhK1roECBAgQIAAAQIECBAgQGB0gcbOEAA0RmthAgQIECBAgAABAgQIECAwqkBzxwsAmrO1MgECBAgQIECAAAECBAgQGE2gwaMFAA3iWpoAAQIECBAgQIAAAQIECIwi0OSxAoAmda1NgAABAgQIECBAgAABAgT2LtDokQKARnktToAAAQIECBAgQIAAAQIE9irQ7HECgGZ9rU6AAAECBAgQIECAAAECBPYm0PBRAoCGgS1PgAABAgQIECBAgAABAgT2ItD0MQKApoWtT4AAAQIECBAgQIAAAQIELi3Q+BECgMaJbUCAAAECBAgQIECAAAECBC4l0Pz9AoDmje1AgAABAgQIECBAgAABAgQuLtDCvQKAFpBtQYAAAQIECBAgQIAAAQIELibQxn0CgDaU7UGAAAECBAgQIECAAAECBHYXaOUeAUArzDYhQIAAAQIECBAgQIAAAQK7CbRz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NYFhGPzH6onM411XGSwz2Pm/g7Xv2VxZe9768tpvnFw++jdT+4/VxgSmL6CCDggIADowJCUSIECAAAECBAgQIEAgbwHVdUFAANCFKamRAAECBAgQIECAAAECOQuorRMCAoBOjEmRBAgQIECAAAECBAgQyFdAZd0QEAB0Y06qJECAAAECBAgQIECAQK4C6uqIgACgI4NSJgECBAgQIECAAAECBPIUUFVXBAQAXZmUOgkQIECAAAECBAgQIJCjgJo6IyAA6MyoFEqAAAECBAgQIECAAIH8BFTUHQEBQHdmpVICBAgQIECAAAECBAjkJqCeDgkIADo0LKUSIECAAAECBAgQIEAgLwHVdElAANClaamVAAECBAgQIECAAAECOQmopVMCAoBOjUuxBAgQIECAAAECBAgQyEdAJd0SEAB0a16qJUCAAAECBAgQIECAQC4C6uiYgACgYwNTLgECBAgQIECAAAECBPIQUEXXBAQAXZuYegkQIECAAAECBAgQIJCDgBo6JyAA6NzIFEyAAAECBAgQIECAAIHpC6igewICgO7NTMUECBAgQIAAAQIECBCYtoD9OyggAOjg0JRMgAABAgQIECBAgACB6QrYvYsCAoAuTk3NBAgQIECAAAECBAgQmKaAvTspIADo5NgUTYAAAQIECBAgQIAAgekJ2LmbAgKAbs5N1QQIECBAgAABAgQIEJiWgH07KiAA6OjglE2AAAECBAgQIECAAIHpCNi1qwICgK5OTt0ECBAgQIAAAQIECBCYhoA9OysgAOjs6BROgAABAgQIECBAgACB9gXs2F0BAUB3Z6dyAgQIECBAgAABAgQItC1gvw4LCAA6PDylEyBAgAABAgQIECBAoF0Bu3VZQADQ5empnQABAgQIECBAgAABAm0K2KvTAgKATo9P8QQIECBAgAABAgQIEGhPwE7dFhAAdHt+qidAgAABAgQIECBAgEBbAvbpuIAAoOMDVD4BAgQIECBAgAABAgTaEbBL1wUEAF2foPoJECBAgECDAoOUnjy7ML/kymCcx8DMwtyNMwuH5g8uzj1lZnH+u2YXDn3zwcXDj5u54fCnNPiwtTQBAk0JWLfzAgKAzo9QAwQIECBAoEGBGJ4cYni2K4NxHgMxxptiHBwdhPgzMYSXhzj49UFIr4sp/cPM4txHqlDgDbML88erj087eMOhrwrXXTfT4KPZ0gQITCjg9O4LDLrfgg4IECBAgAABAgS6JhBDvE8M4bEhhh+sPt42SIPXzuw//aGZhbnXH1yce9bB6w99RbjmmgNd60u9BAoW0FoBAgKAAoaoBQIECBAgQIBACQJVEHBZjPHfDEL88cFg8N9nPuGyD88uzr3q4PVz3xcOHbqyhB71QKC7AiovQWBQQhN6IECAAAECBAgQKE8gxjATQvyGwSC+dGZ28M8zi/Mvn71h/lvC0tWXBRcCBNoVsFsRAgKAIsaoCQIECBAgQIBA2QLbYUAM4btCCr82e+dVd1RhwG1+mWDZM9ddXgKqKUNAAFDGHHVBgAABAgQIEOiPQAyfUIUBTwvD4TtmF+ZfcvkNT/3M/jSvUwJTEbBpIQICgEIGqQ0CBAgQIECAQN8EYowHQgxPHgwHfzGzMP8rszfMP6pvBvol0I6AXUoREACUMkl9ECBAgAABAgR6KhDPXsK/Cym8aWZh7pfD4qGH9JRC2wSaEbBqMQICgGJGqRECBAgQIECAAIEqC/j3MyH+1czi/HXh6qv3ESFAYHIBK5QjIAAoZ5Y6IUCAAAECBAgQqARiiPeJIdwy+7Cr3jrz9EOPqW7yRoDA+ALOLEhAAFDQMLVCgAABAgQIECBwD4EY/lUYxDfMLMzdFJaW9t/jHp8SILBnAQeWJCAAKGmaeiFAgAABAgQIELiXQDx3uXHmxPt+f+a6H/rke93pCwIELi3giKIEBABFjVMzBAgQIECAAAECOwnEEL407tv/1suvn3/CTve7jQCBnQXcWpaAAKCseeqGAAECBAgQIEBgN4EYHjCI6VUHF+aeudshbidA4F4CvihMQABQ2EC1Q4AAAQIECBAgsLvA9k8EDGJ87uzi3M+EpSV/F96dyj0EQggQShPwf3qlTVQ/BAgQIECAAAECexCIT5m9832/EZaumd3DwQ4h0E8BXRcnIAAobqQaIkCAAAECBAgQ2JNADE+YPXHg98PioYfs6XgHEeiZgHbLExAAlDdTHREgQIAAAQIECOxZIH7BTIp/dPDap376nk9xIIF+COiyQAEBQIFD1RIBAgQIECBAgMDeBWKMnxwP7HtNeObcJ+79LEcSKF1AfyUKCABKnKqeCBAgQIAAAQIERhKIITxi9nT8b2Fh4YqRTnQwgVIF9FWkgACgyLFqigABAgQIECBAYGSBGL5wJmz+elha2j/yuU4gUJiAdsoUEACUOVddESBAgAABAgQIjCEQY/jqmRPv+8Xq1FhdvRHoq4C+CxUQABQ6WG0RIECAAAECBAiMJ1A987/64ML8j453trMIlCCgh1IFBAClTlZfBAgQIECAAAECYwvEGJYOXn/oK8ZewIkEuiyg9mIFBADFjlZjBAgQIECAAAEC4wrEEAaDQbw9XH/NA8ddw3kEuiqg7nIFBADlzlZnBAgQIECAAAECEwnEh8wMDrx8oiWcTKB7AiouWEAAUPBwtUaAAAECBAgQIDCZQAzxa2YW52+YbBVnE+iSgFpLFhAAlDxdvREgQIAAAQIECEwukNJPHrz2qZ8++UJWINABASUWLSAAKHq8miNAgAABAgQIEJhUIMZ4IB4YHJ90HecT6IKAGssWEACUPV/dESBAgAABAgQI1CBw9kcBFua+vYalLEEgZwG1FS4gACh8wNojQIAAAQIECBCoTeAF4fDhy2tbzUIEshNQUOkCAoDSJ6w/AgQIECBAgACBWgRijA+dvTz9SC2LWYRAjgJqKl5AAFD8iDVIgAABAgQIECBQl0AKYSE846n3r2s96xDISUAt5QsIAMqfsQ4JECBAgAABAgRqEogxzMye2fe0mpazDIGcBNTSAwEBQA+GrEUCBAgQIECAAIEaBWL6j+G662ZqXNFSBDIQUEIfBAQAfZiyHgkQIECAAAECBGoUiPef2X/6B2pc0FIEpi+ggl4ICAB6MWZNEiBAgAABAgQI1CqQ0o1hacnfpWtFtdg0BezdDwH/p9WPOeuSAAECBAgQIECgRoEY4yfPnHjf1TUuaSkC0xSwd08EBAA9GbQ2CRAgQIAAAQIE6hWIKTy53hWtRmBaAvbti4AAoC+T1icBAgQIECBAgECtAimkfxsWfujBtS5qMQLTELBnbwQEAL0ZtUYJECBAgAABAgTqFIgx7psJ+76nzjWtRWAaAvbsj4AAoD+z1ikBAgQIECBAgEDNAjFEAUDNppZrXcCGPRIQAPRo2FolQIAAAQIECOEfQ1kAABAASURBVBCoWSCGL7zs+sP/quZVLUegRQFb9UlAANCnaeuVAAECBAgQIECgdoFBTN9e+6IWJNCWgH16JSAA6NW4NUuAAAECBAgQIFC3QAzhq+te03oE2hKwT78EBAD9mrduCRAgQIAAAQIE6haI6TFh6UkH617WegRaELBFzwQEAD0buHYJECBAgAABAgTqFYghHjx455WPqXdVqxFoQ8AefRMQAPRt4volQIAAAQIECBCoXaD6S7UfA6hd1YKNC9igdwLV/1f1rmcNEyBAgAABAgQIEKhb4MvqXtB6BJoWsH7/BAQA/Zu5jgkQIECAAAECBOoX+Jz6l7QigUYFLN5DAQFAD4euZQIECBAgQIAAgZoFYvgX4dChK2te1XIEGhSwdB8FBAB9nLqeCRAgQIAAAQIEahc4OBO+ILgQ6IqAOnspIADo5dg1TYAAAQIECBAgULfAIAY/BlA3qvUaE7BwPwUEAP2cu64JECBAgAABAgRqFkghflrNS1qOQFMC1u2pgACgp4PXNgECBAgQIECAQL0CMcQH17ui1Qg0JWDdvgoIAPo6eX0TIECAAAECBAjULSAAqFvUes0IWLW3AgKA3o5e4wQIECBAgAABAnUKpBgeVOd61iLQlIB1+ysgAOjv7HVOgAABAgQIECBQq0DyHQC1elqsIQHL9lhAANDj4WudAAECBAgQIECgToH4qXWuZi0CzQhYtc8CAoA+T1/vBAgQIECAAAECtQnEEAbh6qv31baghQg0IWDNXgsIAHo9fs0TIECAAAECBAjUKvCQh+yvdT2LEahZwHL9FhAA9Hv+uidAgMBYAjHFU2Od6CQCBAiULvCJJ30HQOkz7nZ/qu+5gACg5w8A7RMgQGAcgRTDneOc5xwCBAgQIEBgmgL27ruAAKDvjwD9EyBAYAyBmJIAYAw3pxAgQIAAgakK2Lz3AgKA3j8EABAgQGB0gaHvABgdzRkECBAgQGDKArYnIADwGCBAgACBkQVi8iMAI6M5gQABAgQITFfA7gSCAMCDgAABAgRGFvA7AEYmcwIBAgQIEJiygO0JBAGABwEBAgQIjC6QhvHdo5/lDAIECBAgQGBqAjYmUAn4DoAKwRsBAgQIjCgwHP71iGc4nAABAgQIEJiigK0JbAsIALYVXAkQIEBgJIGT/3jH36aUtkY6ycEECBAgQIDAtATsS+CsgADgLIN3BAgQIDCSwO23bz/5/5uRznEwAQIECBAgMCUB2xI4JyAAOOfgPQECBAiMKBBD9GMAI5o5nAABAgQITEXApgTuEhAA3AXhAwECBAiMJpBi+KvRznA0AQIECBAgMA0BexL4mIAA4GMSPhIgQIDASAIpDv94pBMcTIAAAQIECExDwJ4E7hYQANxN4RMCBAgQGEVgM4XfGeV4xxIgQIAAAQLTELAngY8LCAA+buEzAgQIEBhFYPnYe1JKfgxgFDPHEiBAgACBtgXsR+AeAgKAe2D4lAABAgRGE4gxvC64ECBAgAABAtkKKIzAPQUEAPfU8DkBAgQIjCSQUvjdkU5wMAECBAgQINCmgL0I3EtAAHAvDl8QIECAwCgCG1uXvWaU4x1LgAABAgQItClgLwL3FhAA3NvDVwQIECAwisCtt34gpfSHo5ziWAIECBAgQKAlAdsQOE9AAHAeiC8JECBAYFSB9AujnuF4AgQIECBAoHkBOxA4X0AAcL6IrwkQIEBgJIGN/ellKaWtkU5yMAECBAgQINC0gPUJXCAgALiAxA0ECBAgMJLATS/6YIzhN0c6x8EECBAgQIBAwwKWJ3ChgADgQhO3ECBAgMCIAilEPwYwopnDCRAgQIBAowIWJ7CDgABgBxQ3ESBAgMBoAhtX3PlrIaT3j3aWowkQIECAAIGmBKxLYCcBAcBOKm4jQIAAgdEEll66OQzhhaOd5GgCBAgQIECgIQHLEthRQACwI4sbCRAgQGBUgc19w6MphY1Rz3M8AQIECBAgULeA9QjsLCAA2NnFrQQIECAwqsBNL/pgiOnYqKc5ngABAgQIEKhZwHIEdhEQAOwC42YCBAgQGF0gxnhLCuHU6Gc6gwABAgQIEKhLwDoEdhMQAOwm43YCBAgQGFlg/cjqu2JIPzfyiU4gQIAAAQIE6hKwDoFdBQQAu9K4gwABAgTGEjh9+lkppPWxznUSAQIECBAgMKGA0wnsLiAA2N3GPQQIECAwhsD6bcffnVL4yTFOdQoBAgQIECAwqYDzCVxEQABwERx3ESBAgMB4ApsfOb2SQnrHeGc7iwABAgQIEBhXwHkELiYgALiYjvsIECBAYDyB48dPpxiuGe9kZxEgQIAAAQJjCjiNwEUFBAAX5XEnAQIECIwrsHlk7XdSSr867vnOI0CAAAECBEYVcDyBiwsIAC7u414CBAgQmEBgI6ZDIaX3TbCEUwkQIECAAIG9CjiOwCUEBACXAHI3AQIECEwgsHzsPcNB+O5UXSZYxakECBAgQIDAHgQcQuBSAgKASwm5nwABAgQmEtj+UYBqgSPV1RsBAgQIECDQnICVCVxSQABwSSIHECBAgMCkAhtXPuhHUwhvnHQd5xMgQIAAAQK7CbidwKUFBACXNnIEAQIECEwqsLR0JsT4HSGFDwUXAgQIECBAoH4BKxLYg4AAYA9IDiFAgACByQU2jhz9x2EafnMK4czkq1mBAAECBAgQuKeAzwnsRUAAsBclxxAgQIBALQKbNx97Q7XQE1N1qT56I0CAAAECBOoRsAqBPQkIAPbE5CACBAgQqEtgY3n19hjD4brWsw4BAgQIECBAgMDeBAQAe3NyFAECBAjUKLC+vLaWUlipcUlLESBAgACB/gronMAeBQQAe4RyGAECBAjUK7CxsroYQnppvatajQABAgQI9E9AxwT2KiAA2KuU4wgQIECgdoH15bWnpBCO1L6wBQkQIECAQH8EdEpgzwICgD1TOZAAAQIEGhBIG8urN6Y0PJyqSwPrW5IAAQIECBQuoD0CexcQAOzdypEECBAg0JDAxsqx1WrpJyb/RGDF4I0AAQIECIwg4FACIwgIAEbAcigBAgQINCewsbL28mEYfmNK4URzu1iZAAECBAiUJaAbAqMICABG0XIsAQIECDQqcHL52GuGg60vDiH9WaMbWZwAAQIECJQhoAsCIwkIAEbicjABAgQINC1w8siL/nJ9c/DFKYVjTe9lfQIECBAg0G0B1RMYTUAAMJqXowkQIECgDYGjR09urKzObaX4TSGkD7axpT0IECBAgEDnBBRMYEQBAcCIYA4nQIAAgfYETq4cfdX6/sFnVjv+XHX1RoAAAQIECNxDwKcERhUQAIwq5ngCBAgQaFfgeUfvWF9e/b60NXxsSOHP293cbgQIECBAIFsBhREYWUAAMDKZEwgQIEBgGgIbtxz7g/V3vvfzUkrXp5A+Oo0a7EmAAAECBPIRUAmB0QUEAKObOYMAAQIEpiVw++1bGytrt2ycuezhIYXn+P0A0xqEfQkQIEBg6gIKIDCGgABgDDSnECBAgMCUBW699QPrK6vPWk8zD00hPL26/uOUK7I9AQIECBBoVcBmBMYREACMo+YcAgQIEMhDYGXlxMby6q3V9aHDkL4/pPC/8yhMFQQIECBAoFEBixMYS0AAMBabkwgQIEAgN4HN5bWXrK+sfu6ZQfi8FMKR6uq7AnIbknoIECBAoCYByxAYT0AAMJ6bswgQIEAgU4FTz1/9043l1Rur66cOQ3x8COmnQwofCi4ECBAgQKAUAX0QGFNAADAmnNMIECBAIHuBtLl89PXry2s/uP7O9z4wxfhlKYTFlNIrq0DgA9lXr0ACBAgQILCLgJsJjCsgABhXznkECBAg0B2B7X894MjRN24sr65srKx9y/rK6iee3opfMAzx+6pA4PkhpFdXH9/ZnYZUSoAAAQI9FtA6gbEFBABj0zmRAAECBLoscPqWo2/ZXD76c1Ug8Iz15bVvrD4+fP2KU1dsf6fA1jB8QwrhO7cDgjAMT01xuDBM4VkhhR93ZTDWYyCk93f5vxe1EyCQk4BaCIwvIAAY386ZBAgQIFCawNLx9Y0jR9948ubV39xYXv2V7YBg/ebVF28cOXbz5srqc9ZXVpdcGYzzGKgCpfeV9p+LfggQmJKAbQlMICAAmADPqQQIECBAgAABAgQIEGhTwF4EJhEQAEyi51wCBAgQIECAAAECBAi0J2AnAhMJCAAm4nMyAQIECBAgQIAAAQIE2hKwD4HJBAQAk/k5mwABAgQIECBAgAABAu0I2IXAhAICgAkBnU6AAAECBAgQIECAAIE2BOxBYFIBAcCkgs4nQIAAAQIECBAgQIBA8wJ2IDCxgABgYkILECBAgAABAgQIECBAoGkB6xOYXEAAMLmhFQgQIECAAAECBAgQINCsgNUJ1CAgAKgB0RIECBAgQIAAAQIECBBoUsDaBOoQEADUoWgNAgQIECBAgAABAgQINCdgZQK1CAgAamG0CAECBAgQIECAAAECBJoSsC6BegQEAPU4WoUAAQIECBAgQIAAAQLNCFiVQE0CAoCaIC1DgAABAgQIECBAgACBJgSsSaAuAQFAXZLWIUCAAAECBAgQIECAQP0CViRQm4AAoDZKCxEgQIAAAQIECBAgQKBuAesRqE9AAFCfpZUIECBAgAABAgQIECBQr4DVCNQoIACoEdNSBAgQIECAAAECBAgQqFPAWgTqFBAA1KlpLQIECBAgQIAAAQIECNQnYCUCtQoIAGrltBgBAgQIECBAgAABAgTqErAOgXoFBAD1elqNAAECBAgQIECAAAEC9QhYhUDNAgKAmkEtR4AAAQIECBAgQIAAgToErEGgbgEBQN2i1iNAgAABAgQIECBAgMDkAlYgULuAAKB2UgsSIECAAAECBAgQIEBgUgHnE6hfQABQv6kVCRAgQIAAAQIECBAgMJmAswk0ICAAaADVkgQIECBAgAABAgQIEJhEwLkEmhAQADShak0CBAgQIECAAAECBAiML+BMAo0ICAAaYbUoAQIECBAgQIAAAQIExhVwHoFmBAQAzbhalQABAgQIECBAgAABAuMJOItAQwICgIZgLUuAAAECBAgQIECAAIFxBJxDoCkBAUBTstYlQIAAgU4JHHzGDz388oXD3zizMLc4szj//JmF+aOzC/MvqT5/+ezi3Curr39nZnH+da4MxnkMhBQ/tVP/QSh2fIEPX5HGP9mZBM4KeEegMQEBQGO0FiZAgACBXAUuu3Huc2YWDs3PLs797MzC3BurJ/d3Drb2/+2+mH4jxngkhnBDjGE+xPDk6vPvCiF+U/X1V1efP841MAhjGMQwE1z6IXDfE9X/TfSjVV02JWBdAs0JCACas7UyAQIECGQicPDap376wcW5H5hZnP/F2cW5d+8fxv8d4+BoCPFJMcYviTFcEVwIECBAgEAOAmog0KCAAKBBXEsTIECAwJQElpYGBxcPf2X1yv7yzOL8Xw0O7PubQYhdsXyBAAAQAElEQVQ/FUP4DyHEhwQXAgQIECCQqYCyCDQpMGhycWsTIECAAIE2BWYXDn3zzML8z8+euON9g5B+r3plf6F60v8ZbdZgLwIECBAgMIGAUwk0KiAAaJTX4gQIECDQuMDS1ZfNXj9/qHri/44QB78eY/ieEOL9gwsBAgQIEOicgIIJNCsgAGjW1+oECBAg0JTA4cOfcHBh7pmzJ676+zAIazGGT2tqK+sSIECAAIFWBGxCoGEBAUDDwJYnQIAAgZoFlpYGs4tzc7OXp78dxPjcavWrqqs3AgQIECDQeQENEGhaQADQtLD1CRAgQKA2gZnF+cfOnrjjrSHE1RDDA4ILAQIECBAoR0AnBBoXEAA0TmwDAgQIEJhUYOaGw58yszB3ewzhDSHEzwkuBAgQIECgOAENEWheQADQvLEdCBAgQGACgYML898fhultMcbvnGAZpxIgQIAAgbwFVEegBQEBQAvItiBAgACBMQRuvOa+M4vzvzqI4adjDFeMsYJTCBAgQIBAZwQUSqANAQFAG8r2IECAAIGRBGZuOPzoma0DfxpD+LbgQoAAAQIEyhfQIYFWBAQArTDbhAABAgT2JLD9G/4X5p8dhsM3xBgfGlwIECBAgEAvBDRJoB0BAUA7znYhQIAAgUsJXHPNgZk77/gvIYal6sn/vuBCgAABAgT6IqBPAi0JCABagrYNAQIECFxE4LrrZmbue+A3qyf+33qRo9xFgAABAgSKFNAUgbYEBABtSduHAAECBHYWWFi4Ymb/6dfGEL9m5wPcSoAAAQIEihbQHIHWBAQArVHbiAABAgQuELj22vvNho3XxxAefcF9biBAgAABAr0Q0CSB9gQEAO1Z24kAAQIE7ilww1PuM7v/zOtCjF98z5t9ToAAAQIEeiWgWQItCggAWsS2FQECBAjcJbB09WUzw5n/GmL4/Ltu8YEAAQIECPRSQNME2hQQALSpbS8CBAgQCGH7n/o78eBfiTE+BgcBAgQIEOi5gPYJtCogAGiV22YECBAgMHvijp8JIX5TcCFAgAABAr0XAECgXQEBQLvediNAgECvBWYX554TQnxScCFAgAABAgRCYECgZQEBQMvgtiNAgEBfBWYX5r41hPijwYUAAQIECBA4K+AdgbYFBABti9uPAAECPRQ4+IwfeniK4ed72LqWCRAgQIDAbgJuJ9C6gACgdXIbEiBAoGcCS0v7B2f2vyKGeJ+eda5dAgQIECBwEQF3EWhfQADQvrkdCRAg0CuBmTvveK5/7q9XI9csAQIECOxFwDEEpiAgAJgCui0JECDQF4GDNxz6qhjjYl/61ScBAgQIENirgOMITENAADANdXsSIECgDwJL18wOhoOX9aFVPRIgQIAAgREFHE5gKgICgKmw25QAAQLlC8zeedkPhxj+Rfmd6pAAAQIECIwq4HgC0xEQAEzH3a4ECBAoWmDm6YcemkJYKLpJzREgQIAAgXEFnEdgSgICgCnB25YAAQJFCwziC2IMlxfdo+YIECBAgMCYAk4jMC2BwbQ2ti8BAgQIlCkwszj/2Bjjt5fZna4IECBAgMDEAhYgMDUBAcDU6G1MgACBMgViSC8sszNdESBAgACBOgSsQWB6AgKA6dnbmQABAsUJXH79/NeHEL8ouBAgQIAAAQI7C7iVwBQFBABTxLc1AQIEShMYxPSs0nrSDwECBAgQqFPAWgSmKSAAmKa+vQkQIFCQwMHFw4+LMT6moJa0QoAAAQIE6hawHoGpCggApspvcwIECJQjMEjpR8vpRicECBAgQKAJAWsSmK6AAGC6/nYnQIBAEQIHF+Y/LcTwb4toRhMECBAgQKApAesSmLKAAGDKA7A9AQIEShAYxPSkEvrQAwECBAgQaFLA2gSmLSAAmPYE7E+AAIECBFKK31tAG1ogQIAAAQJNClibwNQFBABTH4ECCBAg0G2BmYW5L48xfFq3u1A9AQIECBBoWsD6BKYvIACY/gxUQIAAgY4LxO/peAPKJ0CAAAECzQvYgUAGAgKADIagBAIECHRWYGlpEEN4YmfrVzgBAgQIEGhJwDYEchAQAOQwBTUQIECgowIHT7z/K0MM9wsuBAgQIECAwMUE3EcgCwEBQBZjUAQBAgS6KjB8XFcrVzcBAgQIEGhPwE4E8hAQAOQxB1UQIECgkwIxRQFAJyenaAIECBBoVcBmBDIREABkMghlECBAoHMCS0v7Q0yP6VzdCiZAgAABAi0L2I5ALgICgFwmoQ4CBAh0TGDmxPu+LIZ4sGNlK5cAAQIECLQtYD8C2QgIALIZhUIIECDQLYEUkm//79bIVEuAAAECUxGwKYF8BAQA+cxCJQQIEOiUQEzhCztVsGIJECBAgMA0BOxJICMBAUBGw1AKAQIEuiQQY3hkl+pVKwECBAgQmIaAPQnkJCAAyGkaaiFAgECHBFKKn92hcpVKgAABAgSmIWBPAlkJCACyGodiCBAg0BGBZx5+UIzh8o5Uq0wCBAgQIDAlAdsSyEtgkFc5qiFAgACBLgjMbIVHdKFONRIgQIAAgakK2JxAZgICgMwGohwCBAh0QmA4/BedqFORBAgQIEBgigK2JpCbgAAgt4mohwABAh0QSCl8QgfKVCIBAgQIEJimgL0JZCcgAMhuJAoiQIBA/gJxEAUA+Y9JhQQIECAwVQGbE8hPQACQ30xURIAAgewFfAdA9iNSIAECBAhMW8D+BDIUEABkOBQlESBAIHeBGNJ9c69RfQQIECBAYJoC9iaQo4AAIMepqIkAAQKZC6TgdwBkPiLlESBAgMB0BexOIEsBAUCWY1EUAQIE8haIIV6Rd4WqI0CAwLQETg2ntbN9cxJQC4E8BQQAec5FVQQIEMhbIKaYd4GqI0CAQPsCKYRhWHrpZvs72zE7AQURyFRAAJDpYJRFgAABAgQIECDQLYEY0ke7VbFqmxKwLoFcBQQAuU5GXQQIECBAgAABAp0SSCnc2amCFduUgHUJZCsgAMh2NAojQIAAAQIECBDolkD0HQDdGlhD1VqWQL4CAoB8Z6MyAgQIECBAgACBLglE3wHQpXE1VquFCWQsIADIeDhKI0CAAAECBAgQ6JKA3wHQpWk1Vat1CeQsIADIeTpqI0CAAAECBAgQ6JLAu7pUrFobEbAogawFBABZj0dxBAgQIECAAAECXRFIIbytK7WqsykB6xLIW0AAkPd8VEeAAAECBAgQINARgThMAoCOzKqxMi1MIHMBAUDmA1IeAQIECBAgQIBANwS29se/6EalqmxKwLoEchcQAOQ+IfURIECAAAECBAhkL5BCGJ6aeZAAIPtJNVqgxQlkLyAAyH5ECiRAgAABAgQIEMhdIKbwp2FpaZh7neprUsDaBPIXEADkPyMVEiBAgAABAgQIZC6QYvi9zEtUXtMC1ifQAQEBQAeGpEQCBAgQIECAAIHMBYZDAUDmI2q6POsT6IKAAKALU1IjAQIECBAgQIBA1gIbw8tfm3WBimtawPoEOiEgAOjEmBRJgAABAgQIECCQq0BK4W3h1ls/kGt96mpDwB4EuiEgAOjGnFRJgAABAgQIECCQqUCMybf/Zzqb1sqyEYGOCAgAOjIoZRIgQIAAAQIECGQr8MpsK1NYKwI2IdAVAQFAVyalTgIECBAgQIAAgQwF0gfX/+6ffzPDwpTUnoCdCHRGQADQmVEplAABAgQIECBAIDeBYYi/HG6/fSu3utTTpoC9CHRHQADQnVmplAABAgQIECBAIDeB4fBluZWknpYFbEegQwICgA4NS6kECBAgQIAAAQL5CKQQ/nHz5mNvyKcilUxDwJ4EuiQgAOjStNRKgAABAgQIECCQk8Av5FSMWqYiYFMCnRIQAHRqXIolQIAAAQIECBDIQaB69X+YhunFOdSihmkK2JtAtwQEAN2al2oJECBAgAABAgRyEEjh5Zs3r70zh1LUMEUBWxPomIAAoGMDUy4BAgQIECBAgMB0BVJ12dqXnjvdKuyeg4AaCHRNQADQtYmplwABAgQIECBAYKoCMYZXnXr+2p9NtQib5yCgBgKdExAAdG5kCiZAgAABAgQIEJimwOkQnj3N/e2di4A6CHRPQADQvZmpmAABAgQIECBAYEoCKYRfP7289uYpbW/bnATUQqCDAgKADg5NyQQIECBAgAABAu0LpJROh63hfPs72zFHATUR6KKAAKCLU1MzAQIECBAgQIBA6wIxhuWNW479Q+sb2zBHATUR6KSAAKCTY1M0AQIECBAgQIBAqwIpvHv9itP/qdU9bZaxgNIIdFNAANDNuamaAAECBAgQIECgRYEUh9eHpePrLW5pq5wF1EagowICgI4OTtkECBAgQIAAAQLtCKSU3rSxfOyX2tnNLl0QUCOBrgoIALo6OXUTIECAAAECBAg0LlA9+b9zuJWe2PhGNuiSgFoJdFZAANDZ0SmcAAECBAgQIECgaYEY0hNP3nrs7U3vY/0uCaiVQHcFBADdnZ3KCRAgQIAAAQIEGhRIIa2trxx7ZYNbWLqLAmom0GEBAUCHh6d0AgQIECBAgACBhgRSeuvGh09f19Dqlu2wgNIJdFlAANDl6amdAAECBAgQIECgdoEUwrvCID4hHD9+uvbFLdh1AfUT6LSAAKDT41M8AQIECBAgQIBAnQIppI8O49ZXrx9ZfVed61qrFAF9EOi2gACg2/NTPQECBAgQIECAQE0C1Sv/Z9IwfcPJIy/6y5qWtExpAvoh0HEBAUDHB6h8AgQIECBAgACB2gSeuHnzsTfUtpqFihPQEIGuCwgAuj5B9RMgQIAAAQIECEwsMAzpRzaWV2+feCELlCygNwKdFxAAdH6EGiBAgAABAgQIEJhEYJjCszaX1547yRrO7YOAHgl0X0AA0P0Z6oAAAQIECBAgQGAMgRTCcBiH37O5svqcMU53St8E9EugAAEBQAFD1AIBAgQIECBAgMBoAimFk8Nh+KbNI8deNtqZju6rgL4JlCAgAChhinogQIAAAQIECBAYQSB9OIT0VSdvXv3NEU5yaL8FdE+gCAEBQBFj1AQBAgQIECBAgMCeBFJ4y3Df1hdsrKz9jz0d7yACZwW8I1CGgACgjDnqggABAgQIECBA4BICKaS19Y+c+pLNm178d5c41N0E7i3gKwKFCAgAChmkNggQIECAAAECBHYWSCnduZXiN20sr82H48dP73yUWwnsLuAeAqUICABKmaQ+CBAgQIAAAQIELhRI4S1p/9bnnlw5+qoL73QLgT0JOIhAMQICgGJGqRECBAgQIECAAIGPC6QPV6/8X7d+5QMf5Vv+P67is3EEnEOgHAEBQDmz1AkBAgQIECBAoPcCKYRhSOGn1oenH7mxsnZbWFo603sUAJMJOJtAQQICgIKGqRUCBAgQIECAQJ8Fqlf8//DMVvyi9ZXVa8LNx9/XZwu91ydgJQIlCQgASpqmXggQIECAAAECfRRI4be2wvDrq1f8H336lqNv6SOBnhsTsDCBMXstlgAAEABJREFUogQEAEWNUzMECBAgQIAAgX4IpBBOhZBeemYYP6d6xf//OLl87DX96FyX7QrYjUBZAgKAsuapGwIECBAgQIBA0QIphbcNQ/qRjTB82Pry2pNP3Xz0z4tuWHPTFbA7gcIEBACFDVQ7BAgQIECAAIHSBKpX+9+VQrr5dBh+4cbK6mdvLq89Nywfe09pfeonPwEVEShNQABQ2kT1Q4AAAQIECBDouED1ZH89pPBbKaUbTm/FL9pYXv2UjeW1hdPLx/5Xx1tTfrcEVEugOAEBQHEj1RABAgQIECBAoGsC6cPVk/3fCyH95DDEx1dP9q/Y/rn+jZW15dO3HP2TqptUXb0RaFnAdgTKExAAlDdTHREgQIAAAQIEshCoXsnfrJ7Uv796cv8P2z+7X33+5urj66qPP13ddt0wxa8NMXzy+vLa/aon+4+rPv7Y5vLR12dRvCIIECBQoIAAoMChaokAAQIECNQlUL0a+2/Wl1ejK4NxHgPVK/kz68trD6ye3H/qxsrqZ1eff3H18auqjz+4sbJ22+bK0d9eP7L6rroer9YhUKeAtQiUKCAAKHGqeiJAgAABAgQIECBAYBIB5xIoUkAAUORYNUWAAAECBAgQIECAwPgCziRQpoAAoMy56ooAAQIECBAgQIAAgXEFnEegUAEBQKGD1RYBAgQIECBAgAABAuMJOItAqQICgFInqy8CBAgQIECAAAECBMYRcA6BYgUEAMWOVmMECBAgQIAAAQIECIwu4AwC5QoIAMqdrc4IECBAgAABAgQIEBhVwPEEChYQABQ8XK0RIECAAAECBAgQIDCagKMJlCwgACh5unojQIAAAQIECBAgQGAUAccSKFpAAFD0eDVHgAABAgQIECBAgMDeBRxJoGwBAUDZ89UdAQIECBAgQIAAAQJ7FXAcgcIFBACFD1h7BAgQIECAAAECBAjsTcBRBEoXEACUPmH9ESBAgAABAgQIECCwFwHHECheQABQ/Ig1SIAAAQIECBAgQIDApQUcQaB8AQFA+TPWIQECBAgQIECAAAEClxJwP4EeCAgAejBkLRIgQIAAAQIECBAgcHEB9xLog4AAoA9T1iMBAgQIECBAgAABAhcTcB+BXggIAHoxZk0SIECAAAECBAgQILC7gHsI9ENAANCPOeuSAAECBAgQIECAAIHdBNxOoCcCAoCeDFqbBAgQIECAAAECBAjsLOBWAn0READ0ZdL6JECAAAECBAgQIEBgJwG3EeiNgACgN6PWKAECBAgQIECAAAECFwq4hUB/BAQA/Zm1TgkQIECAAAECBAgQOF/A1wR6JCAA6NGwtUqAAAECBAgQIECAwL0FfEWgTwICgD5NW68ECBAgQIAAAQIECNxTwOcEeiUgAOjVuDVLgAABAgQIECBAgMDHBXxGoF8CAoB+zVu3BAgQIECAAAECBAh8TMBHAj0TEAD0bODaJUCAAAECBAgQIEDgnID3BPomIADo28T1S4AAAQIECBAgQIDAtoArgd4JCAB6N3INEyBAgAABAgQIECAQAgMC/RMQAPRv5jomQIAAAQIECBAgQIAAgR4KCAB6OHQtEyBAgAABAgQIEOi7gP4J9FFAANDHqeuZAAECBAgQIECAQL8FdE+glwICgF6OXdMECBAgQIAAAQIE+iygdwL9FBAA9HPuuiZAgAABAgQIECDQXwGdE+ipgACgp4PXNgECBAgQIECAAIG+CuibQF8FBAB9nby+CRAgQIAAAQIECPRTQNcEeisgAOjt6DVOgAABAgQIECBAoI8CeibQXwEBQH9nr3MCBAgQIECAAAEC/RPQMYEeCwgAejx8rRMgQIAAAQIECBDom4B+CfRZQADQ5+nrnQABAgQIECBAgEC/BHRLoNcCAoBej1/zBAgQIECAAAECBPokoFcC/RYQAPR7/ronQIAAAQIECBAg0B8BnRLouYAAoOcPAO0TIECAAAECBAgQ6IuAPgn0XUAA0PdHgP4JECBAgAABAgQI9ENAlwR6LyAA6P1DAAABAgQIECBAgACBPgjokQABAYDHAAECBAgQIECAAAEC5QvokACBIADwICBAgAABAgQIECBAoHgBDRIgEAQAHgQECBAgQIAAAQIECBQvoEECBCoB3wFQIXgjQIAAAQIECBAgQKBkAb0RILAtIADYVnAlQIAAAQIECBAgQKBcAZ0RIHBWQABwlsE7AgQIECBAgAABAgRKFdAXAQLnBAQA5xy8J0CAAAECBAgQIECgTAFdESBwl4AA4C4IHwgQIECAAAECBAgQKFFATwQIfExAAPAxCR8JECBAgAABAgQIEChPQEcECNwtIAC4m8InBAgQIECAAAECBAiUJqAfAgQ+LiAA+LiFzwgQIECAAAECBAgQKEtANwQI3ENAAHAPDJ8SIECAAAECBAgQIFCSgF4IELingADgnho+J0CAAAECBAgQIECgHAGdECBwLwEBwL04fEGAAAECBAgQIECAQCkC+iBA4N4CAoB7e/iKAAECBAgQIECAAIEyBHRBgMB5AgKA80B8SYAAAQIECBAgQIBACQJ6IEDgfAEBwPkiviZAgAABAgQIECBAoPsCOiBA4AIBAcAFJG4gQIAAAQIECBAgQKDrAuonQOBCAQHAhSZuIUCAAAECBAgQIECg2wKqJ0BgBwEBwA4obiJAgAABAgQIECBAoMsCaidAYCcBAcBOKm4jQIAAAQIECBAgQKC7AionQGBHAQHAjixuJECAAAECBAgQIECgqwLqJkBgZwEBwM4ubiVAgAABAgQIECBAoJsCqiZAYBcBAcAuMG4mQIAAAQIECBAgQKCLAmomQGA3AQHAbjJuJ0CAAAECBAgQIECgewIqJkBgVwEBwK407iBAgAABAgQIECBAoGsC6iVAYHcBAcDuNu4hQIAAAQIECBAgQKBbAqolQOAiAgKAi+C4iwABAgQIECBAgACBLgmolQCBiwkIAC6m4z4CBAgQIECAAAECBLojoFICBC4qIAC4KI87CRAgQIAAAQIECBDoioA6CRC4uIAA4OI+7iVAgAABAgQIECBAoBsCqiRA4BICAoBLALmbAAECBAgQIECAAIEuCKiRAIFLCQgALiXkfgIECBAgQIAAAQIE8hdQIQEClxQQAFySyAEECBAgQIAAAQIECOQuoD4CBC4tIAC4tJEjCBAgQIAAAQIECBDIW0B1BAjsQUAAsAckhxAgQIAAAQIECBAgkLOA2ggQ2IuAAGAvSo4hQIAAAQIECBAgQCBfAZURILAnAQHAnpgcRIAAAQIECBAgQIBArgLqIkBgbwICgL05OYoAAQIECBAgQIAAgTwFVEWAwB4FBAB7hHIYAQIECBAgQIAAAQI5CqiJAIG9CggA9irlOAIECBAgQIAAAQIE8hNQEQECexYQAOyZyoEECBAgQIAAAQIECOQmoB4CBPYuIADYu5UjCRAgQIAAAQIECBDIS0A1BAiMICAAGAHLoQQIECBAgAABAgQI5CSgFgIERhEQAIyi5VgCBAgQIECAAAECBPIRUAkBAiMJCABG4nIwAQIECBAgQIAAAQK5CKiDAIHRBAQAo3k5mgABAgQIECBAgACBPARUQYDAiAICgBHBHE6AAAECBAgQIECAQA4CaiBAYFQBAcCoYo4nQIAAAQIECBAgQGD6AiogQGBkAQHAyGROIECAAAECBAgQIEBg2gL2J0BgdAEBwOhmziBAgAABAgQIECBAYLoCdidAYAwBAcAYaE4hQIAAAQIECBAgQGCaAvYmQGAcAQHAOGrOIUCAAAECBAgQIEBgegJ2JkBgLAEBwFhsTiJAgAABAgQIECBAYFoC9iVAYDwBAcB4bs4iQIAAAQIECBAgQGA6AnYlQGBMAQHAmHBOI0CAAAECBAgQIEBgGgL2JEBgXAEBwLhyziNAgAABAgQIECBAoH0BOxIgMLaAAGBsOicSIECAAAECBAgQINC2gP0IEBhfQAAwvp0zCRAgQIAAAQIECBBoV8BuBAhMICAAmADPqQQIECBAgAABAgQItClgLwIEJhEQAEyi51wCBAgQIECAAAECBNoTsBMBAhMJCAAm4nMyAQIECBAgQIAAAQJtCdiHAIHJBAQAk/k5mwABAgQIECBAgACBdgTsQoDAhAICgAkBnU6AAAECBAgQIECAQBsC9iBAYFIBAcCkgs4nQIAAAQIECBAgQKB5ATsQIDCxgABgYkILECBAgAABAgQIECDQtID1CRCYXEAAMLmhFQgQIECAAAECBAgQaFbA6gQI1CAgAKgB0RIECBAgQIAAAQIECDQpYG0CBOoQEADUoWgNAgQIECBAgAABAgSaE7AyAQK1CAgAamG0CAECBAgQIECAAAECTQlYlwCBegQEAPU4WoUAAQIECBAgQIAAgWYErEqAQE0CAoCaIC1DgAABAgQIECBAgEATAtYkQKAuAQFAXZLWIUCAAAECBAgQIECgfgErEiBQm4AAoDZKCxEgQIAAAQIECBAgULeA9QgQqE9AAFCfpZUIECBAgAABAgQIEKhXwGoECNQoIACoEdNSBAgQIECAAAECBAjUKWAtAgTqFBAA1KlpLQIECBAgQIAAAQIE6hOwEgECtQoIAGrltBgBAgQIECBAgAABAnUJWIcAgXoFBAD1elqNAAECBAgQIECAAIF6BKxCgEDNAgKAmkEtR4AAAQIECBAgQIBAHQLWIECgbgEBQN2i1iNAgAABAgQIECBAYHIBKxAgULuAAKB2UgsSIECAAAECBAgQIDCpgPMJEKhfQABQv6kVCRAgQIAAAQIECBCYTMDZBAg0ICAAaADVkgQIECBAgAABAgQITCLgXAIEmhAQADShak0CBAgQIECAAAECBMYXcCYBAo0ICAAaYbUoAQIECBAgQIAAAQLjCjiPAIFmBAQAzbhalQABAgQIECBAgACB8QScRYBAQwICgIZgLUuAAAECBAgQIECAwDgCziFAoCkBAUBTstYlQIAAAQIECBAgQGB0AWcQINCYgACgMVoLEyBAgAABAgQIECAwqoDjCRBoTkAA0JytlQkQIECAAAECBAgQGE3A0QQINCggAGgQ19IECBAgQIAAAQIECIwi4FgCBJoUEAA0qWttAgQIECBAgAABAgT2LuBIAgQaFRAANMprcQIECBAgQIAAAQIE9irgOAIEmhUQADTra3UCBAgQIECAAAECBPYm4CgCBBoWEAA0DGx5AgQIECBAgAABAgT2IuAYAgSaFhAANC1sfQIECJQokMKwxLb0dKFA3NrauvBWtxAgQKABAUsSINC4gACgcWIbECBAoDyBFMJ6eV3paCeBM/vMeicXtxEgUL+AFQkQaF5AANC8sR0IECBQnECM4c7imtLQjgKDsO+jO97hRgIECNQrYDUCBFoQEAC0gGwLAgQIlCYQUxQAlDbUXfo5eWpg1rvYuJkAgToFrEWAQBsCAoA2lO1BgACBwgSS7wAobKIXaef+G74D4CI87iJAoCYBy5w7uxsAABAASURBVBAg0IqAAKAVZpsQIECgLIE49CMAZU10525SCMOwdNzve9iZx60ECNQoYCkCBNoREAC042wXAgQIFCUw9B0ARc1zt2ZiEvTsZuN2AgRqFbAYAQItCQgAWoK2DQECBAoTeFdh/WhnB4EUgjnv4OImAgTqFrAeAQJtCQgA2pK2DwECBAoSGAzC2wpqRyu7CURz3o3G7QQI1ChgKQIEWhMQALRGbSMCBAiUI7B+ZPVdKSW/Hb6cke7WiaBnNxm3EyBQm4CFCBBoT0AA0J61nQgQIFCawF+U1pB+7i2QQjTje5P4igCB+gWsSIBAiwICgBaxbUWAAIHCBLw6XNhAz28n+hGA80l8TYBA7QIWJECgTQEBQJva9iJAgEBZAn9UVju6uadACuHMxuxH33rP23xOgACB2gUsSIBAqwICgFa5bUaAAIFyBM7E9HvldKOTCwRSemNYeunmBbe7gQABAjUKWIoAgXYFBADtetuNAAECxQicvuLBb00hfbSYhjRyb4EYBTz3FvEVAQL1C1iRAIGWBQQALYPbjgABAsUILC0NY/IksZh5ntfIMAxff95NviRAgEDNApYjQKBtAQFA2+L2I0CAQEECKSRPEgua58daSSEMT54Ib/jY1z4SIECgEQGLEiDQuoAAoHVyGxIgQKAoAd8mXtQ4zzUTU/qTcOzYnee+8p4AAQLNCFiVAIH2BQQA7ZvbkQABAsUIbLzzn7f/JYD3FtOQRu4SiK+86xMfCBAg0JSAdQkQmIKAAGAK6LYkQIBAMQK3376VQnh5Mf1o5KzA8MzWz5/9xDsCBAg0JmBhAgSmISAAmIa6PQkQIFCQQBwOX1ZQO71vJaX0ps3bXvSO3kMAIECgWQGrEyAwFQEBwFTYbUqAAIFyBNZvPvam6knj28vpqOedxCjQ6flDQPsE2hCwBwEC0xEQAEzH3a4ECBAoSiAGTxpLGGgV5GxtpDO/VEIveiBAIGsBxREgMCUBAcCU4G1LgACBkgS2toa/UFI/fe0lxvDasPLif+5r//omQKAtAfsQIDAtAQHAtOTtS4AAgYIETt567O0hpP9WUEv9bCWFtX42rmsCBFoVsBkBAlMTEABMjd7GBAgQKEtgGNPzyuqoZ92k8Jb1lbVf61nX2iVAYAoCtiRAYHoCAoDp2duZAAECRQlsHjn2uymEPyqqqR41sxXij/SoXa0SIDA9ATsTIDBFAQHAFPFtTYAAgdIEYgz/qbSeetFP9er/yZWjr+pFr5okQGDKArYnQGCaAgKAaerbmwABAoUJrB9Z/bWU0l8V1lbx7WzF4TOKb1KDBAjkIaAKAgSmKiAAmCq/zQkQIFCcQIqDeGNxXRXcUArhjSeXj/3XglvUGgECGQkohQCB6QoIAKbrb3cCBAgUJ7B+ZPUV/kWA7ox1OEw/2p1qVUqAQMcFlE+AwJQFBABTHoDtCRAgUKLA1pl0KKW0VWJvJfV09tX/m9f8840lDVUvBLIWUBwBAtMWEABMewL2J0CAQIECJ2899vYQ4wsLbK2Ylqon/6e20pnvL6YhjRAgkL+ACgkQmLqAAGDqI1AAAQIEyhTYiOvPrjq7o7p6y1PgGadWXvy/8yxNVQQIlCigJwIEpi8gAJj+DFRAgACBMgWOvOSjw5D8dvkMp5tS+r2N5dVbMyxNSQQIlCugMwIEMhAQAGQwBCUQIECgVIHN5bWXhJD8hvmsBpw+uBHTd2VVkmIIEOiBgBYJEMhBQACQwxTUQIAAgYIF1k8f+A/VK87/VHCLnWptKw2+Nywfe0+nilYsAQLdF9ABAQJZCAgAshiDIggQIFCwwG23fSim9O0Fd9iZ1oYp/eeTK0df1ZmCFUqAQDECGiFAIA8BAUAec1AFAQIEihZYv/nYm1IITy+6ydybS+Hdm1eeuDb3MtVHgECRApoiQCATAQFAJoNQBgECBEoXOPdL59JLS+8zx/5SCidPD+M3hqWXbuZYn5oIEChdQH8ECOQiIADIZRLqIECAQA8E1q940PenkH6lB63m1WIM33f6lqN/kldRqiFAoDcCGiVAIBsBAUA2o1AIAQIEeiCwtDTcuOJB3111+prq6q0FgZTSdRvLq7/cwla2IECAwI4CbiRAIB8BAUA+s1AJAQIE+iGwtHRm/Yo7v7V6YvqmfjQ8vS6HITxzY2XttulVYGcCBAgEBAQIZCQgAMhoGEohQIBAbwSWXrq5cebA14UU/ndvem650WFKP7y5vHpTy9vajgABAucJ+JIAgZwEBAA5TUMtBAgQ6JPAbbd9aH2w/pgUwu/3qe2me03VZRjS92+urD2v6b2sT4AAgUsKOIAAgawEBABZjUMxBAgQ6JnAkZd8dGMzfk0I6Td61nkj7W7/tv8YwrdvLq+9pJENLEqAAIERBRxOgEBeAgKAvOahGgIECPRP4OjRk+tXPOhbQwo/1b/m6+s4hfTRFOPXr6+s/Vp9q1qJAAECEwk4mQCBzAQEAJkNRDkECBDopcDS0nB9ZfWaENJP9LL/yZu+Y2sQvnxz+ejrJ1/KCgQIEKhLwDoECOQmIADIbSLqIUCAQI8F1pfXnh1i+PaU0p09Zhip9crqDeH0qc8/9fy1PxvpRAcTIECgaQHrEyCQnYAAILuRKIgAAQL9Flg/svqKtH/rc0NKf9xviYt3Xz3x3wop/PjGlQ963Pptx9998aPdS4AAgfYF7EiAQH4CAoD8ZqIiAgQI9F5g86YX/936lQ96dEphpXqim3oPcj5ACu+uXB6/vrK6FJaWhuff7WsCBAhkIKAEAgQyFBAAZDgUJREgQIBAJbC0dGZjZXUxDNNXVK90/3l1S+/fUgjDYUgvWj8ZP2vz5mNv6D0IAAIEMhZQGgECOQoIAHKcipoIECBA4G6BjVuO/cH6O9/7eSkOF6pXvXv8uwHSm8+E9CWby2uHwtGjH7kbyCcECBDIUUBNBAhkKSAAyHIsiiJAgACBewncfvvWxpFjN8dB/MyUwn+5133Ff5E+HEKaX19ee9Tp5bU3F9+uBgkQKEJAEwQI5CkgAMhzLqoiQIAAgR0E1o+svmtjZfU7hsPhV6YQ/ucOhxRzUxV0bKSQbl4/c9mnV0/+16rGqpar994IECCQv4AKCRDIVEAAkOlglEWAAAECuwts//z7xvLql28NwzdUr44X9ap4Sun09s/5xzOnHrGxvLYQbr31A7tLuIcAAQI5CqiJAIFcBQQAuU5GXQQIECBwSYGTN6/+ZvXq+KOql8a/s+u/KLB64r9VNfxzad++R27/nL9/2q/S8EaAQDcFVE2AQLYCAoBsR6MwAgQIENijQNpYXv2V9ZXVz9kOAqon0r+2x/NyOey9Vd1HhnHfZ68vr37f5vNf+Pe5FKYOAgQIjCPgHAIE8hUQAOQ7G5URIECAwIgC20HAxsrat63v23pASMMfqsKAN1TX6vn1iAs1fXgKHwkp/Owwha9Zv+KBn1TVfePJ5Rf+ddPbWp8AAQItCNiCAIGMBQQAGQ9HaQQIECAwpsBNL/rg+sqx/7yxsvaVGwcGV6UwfGII6SVVEjCVV9erfc9UQcQfVE/6nzMM8fHrHzn1wPWV1adsrqy+NiwtDcfs0mkECBDIUEBJBAjkLCAAyHk6aiNAgACByQWed/SOjeVjv7S+vPb91SvtDzszDJ8dhuGp1RPy/y+F9HeTb3DhCtW6m9X6b0ohvGArpG/cODG8fxVGPLZ60v+szeWjrw/Hj5++8Cy3ECBAoAABLRAgkLWAACDr8SiOAAECBOoWOHXz6tvWb159cfWE/OqN5bVPWz9zYDYMh186DMMnpxRWQkivrp64v360a3rZMKUfrs795q0QH1mtO1Ot/6Uby6vXnlxee3U4duzOuvuwHgECBHIUUBMBAnkLCADyno/qCBAgQKBpgVtv3Vi/+dibNpePvXRjZXVxfXntG6sn7o8f7br2PZsra8+rzv2Nk8tH/6bpkq1PgACBTAWURYBA5gICgMwHpDwCBAgQIECAAAEC3RBQJQECuQsIAHKfkPoIECBAgAABAgQIdEFAjQQIZC8gAMh+RAokQIAAAQIECBAgkL+ACgkQyF9AAJD/jFRIgAABAgQIECBAIHcB9REg0AEBAUAHhqREAgQIECBAgAABAnkLqI4AgS4ICAC6MCU1EiBAgAABAgQIEMhZQG0ECHRCQADQiTEpkgABAgQIECBAgEC+AiojQKAbAgKAbsxJlQQIECBAgAABAgRyFVAXAQIdERAAdGRQyiRAgAABAgQIECCQp4CqCBDoioAAoCuTUicBAgQIECBAgACBHAXURIBAZwQEAJ0ZlUIJECBAgAABAgQI5CegIgIEuiMgAOjOrFRKgAABAgQIECBAIDcB9RAg0CEBAUCHhqVUAgQIECBAgAABAnkJqIYAgS4JCAC6NC21EiBAgAABAgQIEMhJQC0ECHRKQADQqXEplgABAgQIECBAgEA+AiohQKBbAgKAbs1LtQQIECBAgAABAgRyEVAHAQIdExAAdGxgyiVAgAABAgQIECCQh4AqCBDomoAAoGsTUy8BAgQIECBAgACBHATUQIBA5wQEAJ0bmYIJECBAgAABAgQITF9ABQQIdE9AANC9mamYAAECBAgQIECAwLQF7E+AQAcFBAAdHJqSCRAgQIAAAQIECExXwO4ECHRRQADQxampmQABAgQIECBAgMA0BexNgEAnBQQAnRybogkQIECAAAECBAhMT8DOBAh0U0AA0M25qZoAAQIECBAgQIDAtATsS4BARwUEAB0dnLIJECBAgAABAgQITEfArgQIdFVAANDVyambAAECBAgQIECAwDQE7EmAQGcFBACdHZ3CCRAgQIAAAQIECLQvYEcCBLorIADo7uxUToAAAQIECBAgQKBtAfsRINBhAQFAh4endAIECBAgQIAAAQLtCtiNAIEuCwgAujw9tRMgQIAAAQIECBBoU8Be/z879pHbMBDAAND/f3UQBEGKm8qugCXnkGJbWpnDGwkQWFrAALB0fb48AQIECBAgQIAAAQLXCXgSgbUFDABr9+fbEyBAgAABAgQIECBwlYDnEFhcwACweIG+PgECBAgQIECAAAEC1wh4CoHVBQwAqzfo+xMgQIAAAQIECBAgcIWAZxBYXsAAsHyFAhAgQIAAAQIECBAgMF/AEwisL2AAWL9DCQgQIECAAAECBAgQmC3gfAIBAgaAgBJFIECAAAECBAgQIEBgroDTCSQIGAASWpSBAAECBAgQIECAAIGZAs4mECFgAIioUQgCBAgQIECAAAECBOYJOJlAhoABIKNHKQgQIECAAAECBAgQmCXgXAIhAgaAkCLFIECAAAECBAgQIEBgjoBTCaQIGABSmpSDAAECBAgQIECAAIEZAs4kECNgAIipUhACBAgQIECAAAECBMYLOJFAjoABIKdLSQgQIECAAAECBAgQGC3gPAJBAgaAoDJFIUCAAAECBAgQIEBgrIDTCCQJGACS2pSAj1fTAAAEIUlEQVSFAAECBAgQIECAAIGRAs4iECVgAIiqUxgCBAgQIECAAAECBMYJOIlAloABIKtPaQgQIECAAAECBAgQGCXgHAJhAgaAsELFIUCAAAECBAgQIEBgjIBTCKQJGADSGpWHAAECBAgQIECAAIERAs4gECdgAIirVCACBAgQIECAAAECBM4LOIFAnoABIK9TiQgQIECAAAECBAgQOCvgfgKBAgaAwFJFIkCAAAECBAgQIEDgnIC7CSQKGAASW5WJAAECBAgQIECAAIEzAu4lEClgAIisVSgCBAgQIECAAAECBI4LuJNApoABILNXqQgQIECAAAECBAgQOCrgPgKhAgaA0GLFIkCAAAECBAgQIEDgmIC7CKQKGABSm5WLAAECBAgQIECAAIEjAu4hECtgAIitVjACBAgQIECAAAECBPYLuINAroABILdbyQgQIECAAAECBAgQ2CvgegLBAgaA4HJFI0CAAAECBAgQIEBgn4CrCSQLGACS25WNAAECBAgQIECAAIE9Aq4lEC1gAIiuVzgCBAgQIECAAAECBLYLuJJAtoABILtf6QgQIECAAAECBAgQ2CrgOgLhAgaA8ILFI0CAAAECBAgQIEBgm4CrCKQLGADSG5aPAAECBAgQIECAAIEtAq4hEC9gAIivWEACBAgQIECAAAECBN4LuIJAvoABIL9jCQkQIECAAAECBAgQeCfgcwIFAgaAgpJFJECAAAECBAgQIEDgtYBPCTQIGAAaWpaRAAECBAgQIECAAIFXAj4jUCFgAKioWUgCBAgQIECAAAECBJ4L+IRAh4ABoKNnKQkQIECAAAECBAgQeCbgfQIlAgaAkqLFJECAAAECBAgQIEDgsYB3CbQIGABampaTAAECBAgQIECAAIFHAt4jUCNgAKipWlACBAgQIECAAAECBO4FvEOgR8AA0NO1pAQIECBAgAABAgQI/BfwmkCRgAGgqGxRCRAgQIAAAQIECBD4K+AVgSYBA0BT27ISIECAAAECBAgQIPBbwP8EqgQMAFV1C0uAAAECBAgQIECAwI+A/wh0CRgAuvqWlgABAgQIECBAgACBbwF/CZQJGADKCheXAAECBAgQIECAAIEvAb8JtAkYANoal5cAAQIECBAgQIAAgU8BPwTqBAwAdZULTIAAAQIECBAgQIDA7caAQJ+AAaCvc4kJECBAgAABAgQIECBAoFDAAFBYusgECBAgQIAAAQIE2gXkJ9AoYABobF1mAgQIECBAgAABAt0C0hOoFDAAVNYuNAECBAgQIECAAIFmAdkJdAoYADp7l5oAAQIECBAgQIBAr4DkBEoFDAClxYtNgAABAgQIECBAoFVAbgKtAgaA1ublJkCAAAECBAgQINApIDWBWgEDQG31ghMgQIAAAQIECBBoFJCZQK/ABwAAAP//0P7t2QAAAAZJREFUAwBkyX8dV/mvFQAAAABJRU5ErkJggg=="/>
<h1 class="text-headline-md font-headline-md text-primary dark:text-primary-fixed-dim tracking-tight">Finatra</h1>
</div>
<div class="flex items-center gap-4">
<button class="p-2 rounded-full hover:bg-secondary-container/50 transition-colors active:scale-95">
<span class="material-symbols-outlined text-primary">search</span>
</button>
<div class="w-10 h-10 rounded-full overflow-hidden border-2 border-primary/20">
<img alt="User Profile" class="w-full h-full object-cover" data-alt="A professional studio portrait of a friendly young man with short dark hair and a warm smile. He is wearing a clean, modern white t-shirt against a soft, light cream background. The lighting is bright and airy, reflecting a high-end personal finance app's clean and trustworthy light-mode aesthetic with soft teal undertones in the shadows." src="https://lh3.googleusercontent.com/aida-public/AB6AXuBZgrJaSy3bo2z-_3dmrzlTPvy4qYV-rUvDQClhSM5fQ2OkMBvpdidxUuucRjO1l_aqO0IzCuhDUu96nrXJPHg7KD1OznscKT2Bx3JXEHWXiPUzT3LZQdt2NUwgq0xens82ILuZVWbvDFrIHRM2gR3U82UHwbeg60ICNpVGY2JODMPdfeKoQteE9soKpMGXsuvglqba4O-7DMJczL5NplCLXhm6EApEG6wU-sPBusNpUGValyMOBgDaGemOUk2TCDMex8g0Ms-NGD5n"/>
</div>
</div>
</header>
<!-- Main Content Grid -->
<main class="max-w-7xl mx-auto px-margin-mobile md:px-margin-desktop pt-24 pb-8 space-y-gutter">
<!-- Hero Section: Total Balance & Net Worth -->
<section class="grid grid-cols-1 lg:grid-cols-12 gap-gutter">
<!-- Total Balance Card (Sweet Cream) -->
<div class="lg:col-span-7 bg-surface-container-lowest rounded-[2rem] p-8 flex flex-col justify-between min-h-[240px] relative overflow-hidden shadow-sm border border-outline-variant/30">
<div class="relative z-10">
<div class="flex justify-between items-start">
<div>
<p class="text-label-md font-label-md text-on-surface-variant uppercase tracking-widest">Total Balance</p>
<h2 class="text-headline-lg-mobile md:text-headline-lg font-headline-lg text-primary mt-2">$42,590.12</h2>
</div>
<span class="material-symbols-outlined text-primary bg-primary/10 p-3 rounded-2xl">account_balance_wallet</span>
</div>
</div>
<div class="relative z-10 flex gap-4 mt-8">
<div class="flex items-center gap-2 bg-primary text-on-primary px-4 py-2 rounded-full text-label-md font-label-md">
<span class="material-symbols-outlined text-sm">arrow_upward</span>
                        +2.4%
                    </div>
<p class="text-body-md font-body-md text-on-surface-variant flex items-center">from last month</p>
</div>
<!-- Abstract background shape -->
<div class="absolute -bottom-10 -right-10 w-48 h-48 bg-primary/5 rounded-full blur-3xl"></div>
</div>
<!-- Net Worth Snapshot -->
<div class="lg:col-span-5 bg-dark-teal-surface rounded-[2rem] p-8 text-white flex flex-col justify-center gap-6 shadow-xl relative overflow-hidden">
<div class="absolute top-0 right-0 p-6 opacity-20">
<span class="material-symbols-outlined text-6xl">shield_person</span>
</div>
<div>
<p class="text-label-md font-label-md text-primary-fixed opacity-80 uppercase tracking-widest">Net Worth Snapshot</p>
<h3 class="text-headline-md font-headline-md mt-1">$128,402.00</h3>
</div>
<div class="space-y-4">
<div class="flex justify-between items-center text-body-md font-body-md">
<span class="opacity-70">Assets</span>
<span class="font-bold">$142,000.00</span>
</div>
<div class="w-full bg-white/10 h-1.5 rounded-full overflow-hidden">
<div class="bg-primary-fixed h-full w-[85%]"></div>
</div>
<div class="flex justify-between items-center text-body-md font-body-md">
<span class="opacity-70">Liabilities</span>
<span class="font-bold">$13,598.00</span>
</div>
</div>
</div>
</section>
<!-- Insights & Charts -->
<section class="grid grid-cols-1 lg:grid-cols-3 gap-gutter">
<!-- AI Insight Card (Dismissible) -->
<div class="lg:col-span-1 bg-tertiary-fixed rounded-[2rem] p-6 flex flex-col gap-4 relative shadow-sm transition-all duration-300" id="ai-insight">
<div class="flex justify-between items-start">
<div class="flex items-center gap-2 text-on-tertiary-fixed">
<span class="material-symbols-outlined filled-icon">auto_awesome</span>
<span class="text-label-md font-label-md uppercase font-bold">AI Insight</span>
</div>
<button class="text-on-tertiary-fixed/50 hover:text-on-tertiary-fixed transition-colors" onclick="document.getElementById('ai-insight').style.display='none'">
<span class="material-symbols-outlined text-lg">close</span>
</button>
</div>
<p class="text-body-lg font-body-lg text-on-tertiary-fixed-variant leading-relaxed">
                    You've spent <span class="font-bold">15% more</span> on Dining Out than last month. Consider moving <span class="font-bold">$200</span> to your "Travel Fund" to stay on track.
                </p>
<button class="mt-2 self-start px-6 py-2 bg-on-tertiary-fixed text-tertiary-fixed rounded-full text-label-md font-label-md hover:opacity-90 active:scale-95 transition-all">
                    Show Recommendation
                </button>
</div>
<!-- Income vs Expense Bar Chart -->
<div class="lg:col-span-2 bg-surface-container rounded-[2rem] p-8 shadow-sm border border-outline-variant/20">
<div class="flex justify-between items-center mb-6">
<h3 class="text-title-md font-title-md text-on-surface">Income vs Expenses</h3>
<div class="flex gap-4">
<div class="flex items-center gap-2">
<div class="w-3 h-3 bg-primary rounded-full"></div>
<span class="text-label-md font-label-md">Income</span>
</div>
<div class="flex items-center gap-2">
<div class="w-3 h-3 bg-tertiary rounded-full"></div>
<span class="text-label-md font-label-md">Expense</span>
</div>
</div>
</div>
<!-- Mock Bar Chart -->
<div class="flex items-end justify-between h-40 gap-2 px-2">
<!-- Week 1 -->
<div class="flex-1 flex flex-col items-center gap-2 h-full justify-end group">
<div class="w-full flex justify-center items-end gap-1 h-full">
<div class="bg-primary/20 w-3 rounded-t-full transition-all group-hover:bg-primary/40" style="height: 60%;"></div>
<div class="bg-tertiary/20 w-3 rounded-t-full transition-all group-hover:bg-tertiary/40" style="height: 40%;"></div>
</div>
<span class="text-[10px] font-label-md text-on-surface-variant">W1</span>
</div>
<!-- Week 2 -->
<div class="flex-1 flex flex-col items-center gap-2 h-full justify-end group">
<div class="w-full flex justify-center items-end gap-1 h-full">
<div class="bg-primary/20 w-3 rounded-t-full" style="height: 80%;"></div>
<div class="bg-tertiary/20 w-3 rounded-t-full" style="height: 55%;"></div>
</div>
<span class="text-[10px] font-label-md text-on-surface-variant">W2</span>
</div>
<!-- Week 3 (Current) -->
<div class="flex-1 flex flex-col items-center gap-2 h-full justify-end group">
<div class="w-full flex justify-center items-end gap-1 h-full">
<div class="bg-primary w-3 rounded-t-full" style="height: 95%;"></div>
<div class="bg-tertiary w-3 rounded-t-full" style="height: 30%;"></div>
</div>
<span class="text-[10px] font-label-md text-primary font-bold">W3</span>
</div>
<!-- Week 4 -->
<div class="flex-1 flex flex-col items-center gap-2 h-full justify-end group">
<div class="w-full flex justify-center items-end gap-1 h-full">
<div class="bg-primary/20 w-3 rounded-t-full" style="height: 40%;"></div>
<div class="bg-tertiary/20 w-3 rounded-t-full" style="height: 70%;"></div>
</div>
<span class="text-[10px] font-label-md text-on-surface-variant">W4</span>
</div>
</div>
</div>
</section>
<!-- Recent Transactions -->
<section class="bg-surface-container-low rounded-[2rem] p-8 shadow-sm border border-outline-variant/10">
<div class="flex justify-between items-center mb-8">
<h3 class="text-title-md font-title-md text-on-surface">Recent Transactions</h3>
<button class="text-primary text-label-md font-label-md hover:underline">View All</button>
</div>
<div class="space-y-2">
<!-- Transaction Item 1 -->
<div class="flex items-center justify-between p-4 hover:bg-surface-container-high rounded-2xl transition-colors group">
<div class="flex items-center gap-4">
<div class="w-12 h-12 bg-secondary-container flex items-center justify-center rounded-full text-primary">
<span class="material-symbols-outlined">shopping_cart</span>
</div>
<div>
<p class="text-body-lg font-bold text-on-surface">Whole Foods Market</p>
<p class="text-label-md font-label-md text-on-surface-variant">Groceries • Today, 10:45 AM</p>
</div>
</div>
<div class="text-right">
<p class="text-body-lg font-bold text-error">-$84.20</p>
<p class="text-[10px] font-label-md text-on-surface-variant uppercase tracking-tighter">Debit Card • 4492</p>
</div>
</div>
<!-- Transaction Item 2 -->
<div class="flex items-center justify-between p-4 hover:bg-surface-container-high rounded-2xl transition-colors group">
<div class="flex items-center gap-4">
<div class="w-12 h-12 bg-primary/10 flex items-center justify-center rounded-full text-primary">
<span class="material-symbols-outlined">payments</span>
</div>
<div>
<p class="text-body-lg font-bold text-on-surface">Monthly Salary</p>
<p class="text-label-md font-label-md text-on-surface-variant">Income • Yesterday, 9:00 AM</p>
</div>
</div>
<div class="text-right">
<p class="text-body-lg font-bold text-primary">+$4,250.00</p>
<p class="text-[10px] font-label-md text-on-surface-variant uppercase tracking-tighter">Direct Deposit</p>
</div>
</div>
<!-- Transaction Item 3 -->
<div class="flex items-center justify-between p-4 hover:bg-surface-container-high rounded-2xl transition-colors group">
<div class="flex items-center gap-4">
<div class="w-12 h-12 bg-tertiary-fixed flex items-center justify-center rounded-full text-on-tertiary-fixed-variant">
<span class="material-symbols-outlined">directions_car</span>
</div>
<div>
<p class="text-body-lg font-bold text-on-surface">Chevron Gas Station</p>
<p class="text-label-md font-label-md text-on-surface-variant">Transport • Oct 24, 6:30 PM</p>
</div>
</div>
<div class="text-right">
<p class="text-body-lg font-bold text-error">-$56.00</p>
<p class="text-[10px] font-label-md text-on-surface-variant uppercase tracking-tighter">Debit Card • 4492</p>
</div>
</div>
</div>
</section>
</main>
<!-- FAB: Quick Add -->
<button class="fixed bottom-24 right-6 md:bottom-10 md:right-10 w-16 h-16 bg-primary text-white rounded-[1.25rem] shadow-2xl flex items-center justify-center hover:scale-110 active:scale-95 transition-all duration-200 z-50">
<span class="material-symbols-outlined text-3xl font-bold">add</span>
</button>
<!-- BottomNavBar -->
<nav class="md:hidden glass-nav bg-surface/80 dark:bg-surface-dim/80 fixed bottom-0 left-0 w-full h-20 flex justify-around items-center px-2 pb-safe z-40 border-t border-outline-variant/30">
<a class="flex flex-col items-center justify-center bg-secondary-container dark:bg-secondary-container text-on-secondary-container rounded-full px-5 py-1 active:scale-90 transition-all duration-200" href="#">
<span class="material-symbols-outlined filled-icon">home</span>
<span class="text-label-md font-label-md">Home</span>
</a>
<a class="flex flex-col items-center justify-center text-on-secondary-container/70 dark:text-on-secondary-fixed-variant px-5 py-1 hover:bg-surface-container-high transition-colors active:scale-90 transition-all duration-200" href="#">
<span class="material-symbols-outlined">receipt_long</span>
<span class="text-label-md font-label-md">Transactions</span>
</a>
<a class="flex flex-col items-center justify-center text-on-secondary-container/70 dark:text-on-secondary-fixed-variant px-5 py-1 hover:bg-surface-container-high transition-colors active:scale-90 transition-all duration-200" href="#">
<span class="material-symbols-outlined">account_balance</span>
<span class="text-label-md font-label-md">Budgets</span>
</a>
<a class="flex flex-col items-center justify-center text-on-secondary-container/70 dark:text-on-secondary-fixed-variant px-5 py-1 hover:bg-surface-container-high transition-colors active:scale-90 transition-all duration-200" href="#">
<span class="material-symbols-outlined">analytics</span>
<span class="text-label-md font-label-md">Analytics</span>
</a>
<a class="flex flex-col items-center justify-center text-on-secondary-container/70 dark:text-on-secondary-fixed-variant px-5 py-1 hover:bg-surface-container-high transition-colors active:scale-90 transition-all duration-200" href="#">
<span class="material-symbols-outlined">settings</span>
<span class="text-label-md font-label-md">Settings</span>
</a>
</nav>
<!-- Desktop Navigation (Hidden on mobile) -->
<nav class="hidden md:flex fixed top-0 left-0 h-20 items-center justify-center w-full pointer-events-none z-50">
<div class="bg-surface-container/60 backdrop-blur-xl px-8 py-2 rounded-full border border-outline-variant/30 flex gap-8 pointer-events-auto shadow-sm">
<a class="text-primary font-bold flex items-center gap-2" href="#">
<span class="material-symbols-outlined filled-icon">home</span> Home
            </a>
<a class="text-on-surface-variant hover:text-primary transition-colors flex items-center gap-2" href="#">
<span class="material-symbols-outlined">receipt_long</span> Transactions
            </a>
<a class="text-on-surface-variant hover:text-primary transition-colors flex items-center gap-2" href="#">
<span class="material-symbols-outlined">account_balance</span> Budgets
            </a>
<a class="text-on-surface-variant hover:text-primary transition-colors flex items-center gap-2" href="#">
<span class="material-symbols-outlined">analytics</span> Analytics
            </a>
</div>
</nav>
<script>
        // Simple micro-interaction for cards
        document.querySelectorAll('.rounded-\\[2rem\\]').forEach(card => {
            card.addEventListener('mouseenter', () => {
                card.style.transform = 'translateY(-2px)';
                card.style.transition = 'all 0.3s ease';
            });
            card.addEventListener('mouseleave', () => {
                card.style.transform = 'translateY(0px)';
            });
        });
    </script>
</body></html>

<!-- Transactions History -->
<!DOCTYPE html>

<html class="light" lang="en"><head>
<meta charset="utf-8"/>
<meta content="width=device-width, initial-scale=1.0" name="viewport"/>
<title>Finatra Transactions</title>
<script src="https://cdn.tailwindcss.com?plugins=forms,container-queries"></script>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&amp;family=Poppins:wght@600;700&amp;display=swap" rel="stylesheet"/>
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&amp;display=swap" rel="stylesheet"/>
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&amp;display=swap" rel="stylesheet"/>
<style>
        .material-symbols-outlined {
            font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24;
        }
        .swipe-container {
            touch-action: pan-y;
            user-select: none;
        }
        .swipe-action-left, .swipe-action-right {
            position: absolute;
            top: 0;
            bottom: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            width: 80px;
            z-index: 0;
        }
        .swipe-content {
            position: relative;
            z-index: 10;
            transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
        }
        .hide-scrollbar::-webkit-scrollbar {
            display: none;
        }
        .hide-scrollbar {
            -ms-overflow-style: none;
            scrollbar-width: none;
        }
    </style>
<script id="tailwind-config">
        tailwind.config = {
            darkMode: "class",
            theme: {
                extend: {
                    "colors": {
                        "inverse-primary": "#7fd6ca",
                        "tertiary-fixed-dim": "#d7c4aa",
                        "secondary-fixed-dim": "#bacac8",
                        "on-secondary-fixed": "#101e1d",
                        "error-container": "#ffdad6",
                        "surface-dim": "#dfd9d3",
                        "on-surface": "#1e1b18",
                        "dark-teal-surface": "#1A3330",
                        "outline-variant": "#bdc9c6",
                        "primary-fixed": "#9bf2e6",
                        "secondary-container": "#d6e6e4",
                        "on-error-container": "#93000a",
                        "on-tertiary-fixed": "#241a09",
                        "on-primary-fixed-variant": "#00504a",
                        "outline": "#6e7977",
                        "system-red": "#B00020",
                        "secondary-fixed": "#d6e6e4",
                        "primary": "#005b53",
                        "on-background": "#1e1b18",
                        "on-tertiary": "#ffffff",
                        "on-secondary-container": "#586766",
                        "inverse-on-surface": "#f6f0ea",
                        "on-primary-container": "#a1f8ec",
                        "background": "#fff8f2",
                        "error": "#ba1a1a",
                        "on-error": "#ffffff",
                        "tertiary-fixed": "#f4dfc5",
                        "surface-container": "#f3ede7",
                        "ink-text": "#1A1A1A",
                        "on-surface-variant": "#3e4947",
                        "on-secondary": "#ffffff",
                        "on-tertiary-container": "#fae5ca",
                        "primary-fixed-dim": "#7fd6ca",
                        "surface": "#fff8f2",
                        "surface-container-low": "#f9f2ec",
                        "on-primary": "#ffffff",
                        "deep-ink-bg": "#0F1F1E",
                        "tertiary-container": "#756651",
                        "surface-bright": "#fff8f2",
                        "primary-container": "#0a756c",
                        "surface-container-highest": "#e8e1dc",
                        "surface-container-high": "#eee7e1",
                        "on-tertiary-fixed-variant": "#524531",
                        "inverse-surface": "#33302c",
                        "tertiary": "#5c4e3a",
                        "on-primary-fixed": "#00201d",
                        "surface-tint": "#006a62",
                        "secondary": "#526160",
                        "on-secondary-fixed-variant": "#3b4a48",
                        "surface-variant": "#e8e1dc",
                        "surface-container-lowest": "#ffffff"
                    },
                    "borderRadius": {
                        "DEFAULT": "0.25rem",
                        "lg": "0.5rem",
                        "xl": "0.75rem",
                        "full": "9999px"
                    },
                    "spacing": {
                        "margin-tablet": "24px",
                        "stack-sm": "8px",
                        "stack-md": "16px",
                        "gutter": "16px",
                        "stack-lg": "24px",
                        "margin-mobile": "16px",
                        "margin-desktop": "32px"
                    },
                    "fontFamily": {
                        "body-md": ["Inter"],
                        "headline-md": ["Poppins"],
                        "headline-lg": ["Poppins"],
                        "title-md": ["Inter"],
                        "headline-lg-mobile": ["Poppins"],
                        "body-lg": ["Inter"],
                        "label-md": ["Inter"]
                    },
                    "fontSize": {
                        "body-md": ["14px", {"lineHeight": "20px", "fontWeight": "400"}],
                        "headline-md": ["28px", {"lineHeight": "36px", "fontWeight": "600"}],
                        "headline-lg": ["48px", {"lineHeight": "56px", "letterSpacing": "-0.02em", "fontWeight": "700"}],
                        "title-md": ["18px", {"lineHeight": "24px", "fontWeight": "600"}],
                        "headline-lg-mobile": ["32px", {"lineHeight": "40px", "fontWeight": "700"}],
                        "body-lg": ["16px", {"lineHeight": "24px", "fontWeight": "400"}],
                        "label-md": ["12px", {"lineHeight": "16px", "letterSpacing": "0.5px", "fontWeight": "500"}]
                    }
                },
            },
        }
    </script>
<style>
    body {
      min-height: max(884px, 100dvh);
    }
  </style>
  </head>
<body class="bg-background text-on-background font-body-md overflow-x-hidden pb-24">
<!-- Top App Bar -->
<header class="sticky top-0 z-50 bg-surface/80 backdrop-blur-md flex items-center justify-between px-margin-mobile py-stack-sm w-full">
<div class="flex items-center gap-3">
<span class="material-symbols-outlined text-primary text-headline-md">account_balance_wallet</span>
<h1 class="text-headline-md font-headline-md text-primary tracking-tight">Finatra</h1>
</div>
<div class="w-10 h-10 rounded-full bg-secondary-container flex items-center justify-center overflow-hidden active:scale-95 transition-transform cursor-pointer">
<img alt="User profile" class="w-full h-full object-cover" data-alt="A professional headshot of a person with a warm and friendly expression, set against a soft, out-of-focus interior office background. The lighting is natural and flattering, utilizing high-key tones that align with a modern, high-privacy personal finance application. The overall aesthetic is clean, minimalist, and premium, featuring the Deep Teal and Sweet Cream color palette." src="https://lh3.googleusercontent.com/aida-public/AB6AXuDELqKg0HrhK_jJ4gJ_toXHG8DrZhWhRiGdnTXVr1vKBddyWo9qpMV1bFYDeAbRsr5AsOpLrTN0Y84bnEzP4H9MCkT73a8Lxz-94aBoyuSsTA5SrHFoM8qkKoU_Q1DX1BXAqy60on3z26pJhsq35f8K9V4zj-2EgnfC8sSmN1VlhZ7VJ5molAstLiaL3U7Uk9VCmDLswk4xfjDb2JvBt9O-HefKQQtXdF4dTVVHq8lyeB1VQhf3jSmc_6BEhQmBKKk_fwnzUYlyEt60"/>
</div>
</header>
<main class="max-w-2xl mx-auto px-margin-mobile mt-stack-md">
<!-- Search & Filters Container -->
<div class="space-y-stack-md mb-stack-lg">
<!-- Search Bar -->
<div class="relative group">
<div class="absolute inset-y-0 left-4 flex items-center pointer-events-none">
<span class="material-symbols-outlined text-outline">search</span>
</div>
<input class="w-full h-14 pl-12 pr-4 bg-surface-container rounded-xl border-none focus:ring-2 focus:ring-primary text-body-lg placeholder:text-outline transition-all" placeholder="Search transactions..." type="text"/>
<div class="absolute inset-y-0 right-4 flex items-center">
<span class="material-symbols-outlined text-primary cursor-pointer">auto_awesome</span>
</div>
</div>
<!-- Filter Chips -->
<div class="flex gap-2 overflow-x-auto hide-scrollbar pb-2">
<button class="px-4 py-2 bg-primary text-on-primary rounded-full font-label-md text-label-md transition-all shrink-0">All</button>
<button class="px-4 py-2 border border-outline text-on-surface-variant rounded-full font-label-md text-label-md hover:bg-secondary-container/50 transition-all shrink-0">Food &amp; Drink</button>
<button class="px-4 py-2 border border-outline text-on-surface-variant rounded-full font-label-md text-label-md hover:bg-secondary-container/50 transition-all shrink-0">Transport</button>
<button class="px-4 py-2 border border-outline text-on-surface-variant rounded-full font-label-md text-label-md hover:bg-secondary-container/50 transition-all shrink-0">Shopping</button>
<button class="px-4 py-2 border border-outline text-on-surface-variant rounded-full font-label-md text-label-md hover:bg-secondary-container/50 transition-all shrink-0">Subscriptions</button>
</div>
</div>
<!-- Transactions List -->
<div class="space-y-stack-lg">
<!-- Date Group: Today -->
<section>
<h2 class="text-label-md font-label-md text-on-surface-variant uppercase tracking-widest mb-stack-sm px-1">Today</h2>
<div class="bg-surface-container-low rounded-2xl overflow-hidden border border-outline-variant/30">
<!-- Transaction Item: Food -->
<div class="swipe-container relative">
<div class="swipe-action-left bg-error text-on-error">
<span class="material-symbols-outlined">delete</span>
</div>
<div class="swipe-action-right bg-primary text-on-primary">
<span class="material-symbols-outlined">edit</span>
</div>
<div class="swipe-content bg-surface-container-low p-4 flex items-center justify-between border-b border-outline-variant/30 active:bg-surface-container-high transition-colors">
<div class="flex items-center gap-4">
<div class="w-12 h-12 rounded-full bg-secondary-container flex items-center justify-center">
<span class="material-symbols-outlined text-primary">restaurant</span>
</div>
<div>
<h3 class="font-title-md text-title-md text-ink-text">Blue Bottle Coffee</h3>
<p class="text-body-md text-on-surface-variant">Checking account • 09:15 AM</p>
</div>
</div>
<div class="text-right">
<span class="font-title-md text-title-md text-error">-$5.40</span>
</div>
</div>
</div>
<!-- Transaction Item: Salary -->
<div class="swipe-container relative">
<div class="swipe-action-left bg-error text-on-error">
<span class="material-symbols-outlined">delete</span>
</div>
<div class="swipe-action-right bg-primary text-on-primary">
<span class="material-symbols-outlined">edit</span>
</div>
<div class="swipe-content bg-surface-container-low p-4 flex items-center justify-between active:bg-surface-container-high transition-colors">
<div class="flex items-center gap-4">
<div class="w-12 h-12 rounded-full bg-secondary-container flex items-center justify-center">
<span class="material-symbols-outlined text-primary">payments</span>
</div>
<div>
<h3 class="font-title-md text-title-md text-ink-text">Monthly Salary</h3>
<p class="text-body-md text-on-surface-variant">Savings account • 08:00 AM</p>
</div>
</div>
<div class="text-right">
<span class="font-title-md text-title-md text-primary">+$4,200.00</span>
</div>
</div>
</div>
</div>
</section>
<!-- Date Group: Yesterday -->
<section>
<h2 class="text-label-md font-label-md text-on-surface-variant uppercase tracking-widest mb-stack-sm px-1">Yesterday</h2>
<div class="bg-surface-container-low rounded-2xl overflow-hidden border border-outline-variant/30">
<!-- Transaction Item: Grocery -->
<div class="swipe-container relative">
<div class="swipe-action-left bg-error text-on-error">
<span class="material-symbols-outlined">delete</span>
</div>
<div class="swipe-action-right bg-primary text-on-primary">
<span class="material-symbols-outlined">edit</span>
</div>
<div class="swipe-content bg-surface-container-low p-4 flex items-center justify-between border-b border-outline-variant/30 active:bg-surface-container-high transition-colors">
<div class="flex items-center gap-4">
<div class="w-12 h-12 rounded-full bg-secondary-container flex items-center justify-center">
<span class="material-symbols-outlined text-primary">shopping_basket</span>
</div>
<div>
<h3 class="font-title-md text-title-md text-ink-text">Whole Foods Market</h3>
<p class="text-body-md text-on-surface-variant">Checking account • 06:45 PM</p>
</div>
</div>
<div class="text-right">
<span class="font-title-md text-title-md text-error">-$82.12</span>
</div>
</div>
</div>
<!-- Transaction Item: Gas -->
<div class="swipe-container relative">
<div class="swipe-content bg-surface-container-low p-4 flex items-center justify-between border-b border-outline-variant/30 active:bg-surface-container-high transition-colors">
<div class="flex items-center gap-4">
<div class="w-12 h-12 rounded-full bg-secondary-container flex items-center justify-center">
<span class="material-symbols-outlined text-primary">local_gas_station</span>
</div>
<div>
<h3 class="font-title-md text-title-md text-ink-text">Shell Oil</h3>
<p class="text-body-md text-on-surface-variant">Credit Card • 01:20 PM</p>
</div>
</div>
<div class="text-right">
<span class="font-title-md text-title-md text-error">-$45.00</span>
</div>
</div>
</div>
<!-- Transaction Item: Sub -->
<div class="swipe-container relative">
<div class="swipe-content bg-surface-container-low p-4 flex items-center justify-between active:bg-surface-container-high transition-colors">
<div class="flex items-center gap-4">
<div class="w-12 h-12 rounded-full bg-secondary-container flex items-center justify-center">
<span class="material-symbols-outlined text-primary">subscriptions</span>
</div>
<div>
<h3 class="font-title-md text-title-md text-ink-text">Netflix Subscription</h3>
<p class="text-body-md text-on-surface-variant">Credit Card • 10:00 AM</p>
</div>
</div>
<div class="text-right">
<span class="font-title-md text-title-md text-error">-$15.99</span>
</div>
</div>
</div>
</div>
</section>
<!-- Date Group: 12 Oct -->
<section>
<h2 class="text-label-md font-label-md text-on-surface-variant uppercase tracking-widest mb-stack-sm px-1">October 12</h2>
<div class="bg-surface-container-low rounded-2xl overflow-hidden border border-outline-variant/30">
<div class="swipe-container relative">
<div class="swipe-content bg-surface-container-low p-4 flex items-center justify-between active:bg-surface-container-high transition-colors">
<div class="flex items-center gap-4">
<div class="w-12 h-12 rounded-full bg-secondary-container flex items-center justify-center">
<span class="material-symbols-outlined text-primary">flight</span>
</div>
<div>
<h3 class="font-title-md text-title-md text-ink-text">United Airlines</h3>
<p class="text-body-md text-on-surface-variant">Credit Card • 03:30 PM</p>
</div>
</div>
<div class="text-right">
<span class="font-title-md text-title-md text-error">-$425.00</span>
</div>
</div>
</div>
</div>
</section>
</div>
</main>
<!-- FAB -->
<button class="fixed bottom-24 right-6 w-14 h-14 bg-primary text-on-primary rounded-2xl shadow-xl flex items-center justify-center active:scale-95 transition-all z-40">
<span class="material-symbols-outlined text-3xl">add</span>
</button>
<!-- Bottom Navigation Bar -->
<nav class="fixed bottom-0 left-0 w-full h-20 bg-surface/80 backdrop-blur-md border-t border-outline-variant/30 flex justify-around items-center px-2 pb-safe z-50">
<div class="flex flex-col items-center justify-center text-on-secondary-container/70 px-5 py-1 hover:bg-surface-container-high transition-colors active:scale-90 duration-200 cursor-pointer">
<span class="material-symbols-outlined">home</span>
<span class="text-label-md font-label-md">Home</span>
</div>
<div class="flex flex-col items-center justify-center bg-secondary-container text-on-secondary-container rounded-full px-5 py-1 active:scale-90 transition-all duration-200 cursor-pointer">
<span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">receipt_long</span>
<span class="text-label-md font-label-md">Transactions</span>
</div>
<div class="flex flex-col items-center justify-center text-on-secondary-container/70 px-5 py-1 hover:bg-surface-container-high transition-colors active:scale-90 duration-200 cursor-pointer">
<span class="material-symbols-outlined">account_balance</span>
<span class="text-label-md font-label-md">Budgets</span>
</div>
<div class="flex flex-col items-center justify-center text-on-secondary-container/70 px-5 py-1 hover:bg-surface-container-high transition-colors active:scale-90 duration-200 cursor-pointer">
<span class="material-symbols-outlined">analytics</span>
<span class="text-label-md font-label-md">Analytics</span>
</div>
<div class="flex flex-col items-center justify-center text-on-secondary-container/70 px-5 py-1 hover:bg-surface-container-high transition-colors active:scale-90 duration-200 cursor-pointer">
<span class="material-symbols-outlined">settings</span>
<span class="text-label-md font-label-md">Settings</span>
</div>
</nav>
<script>
        document.querySelectorAll('.swipe-container').forEach(container => {
            const content = container.querySelector('.swipe-content');
            let startX = 0;
            let currentX = 0;
            let isDragging = false;

            container.addEventListener('touchstart', (e) => {
                startX = e.touches[0].clientX;
                isDragging = true;
                content.style.transition = 'none';
            });

            container.addEventListener('touchmove', (e) => {
                if (!isDragging) return;
                currentX = e.touches[0].clientX - startX;
                
                // Limit swipe distance
                if (Math.abs(currentX) > 100) {
                    currentX = currentX > 0 ? 100 + (currentX - 100) * 0.2 : -100 + (currentX + 100) * 0.2;
                }
                
                content.style.transform = `translateX(${currentX}px)`;
            });

            container.addEventListener('touchend', () => {
                isDragging = false;
                content.style.transition = 'transform 0.3s cubic-bezier(0.4, 0, 0.2, 1)';
                
                if (currentX > 60) {
                    // Logic for right swipe (e.g., delete)
                    content.style.transform = `translateX(0px)`;
                } else if (currentX < -60) {
                    // Logic for left swipe (e.g., edit)
                    content.style.transform = `translateX(0px)`;
                } else {
                    content.style.transform = `translateX(0px)`;
                }
                currentX = 0;
            });
        });

        // Simple scroll hiding of FAB for better UX
        let lastScroll = 0;
        const fab = document.querySelector('button.fixed');
        window.addEventListener('scroll', () => {
            const currentScroll = window.pageYOffset;
            if (currentScroll > lastScroll && currentScroll > 100) {
                fab.style.transform = 'scale(0) rotate(90deg)';
                fab.style.opacity = '0';
            } else {
                fab.style.transform = 'scale(1) rotate(0deg)';
                fab.style.opacity = '1';
            }
            lastScroll = currentScroll;
        });
    </script>
</body></html>