import { sounds } from './audio.js';

// Balloon Types & Configurations
const BALLOON_TYPES = {
  STANDARD: { name: 'standard', weight: 55, points: 10 },
  GOLDEN: { name: 'golden', weight: 12, points: 50 },
  RAINBOW: { name: 'rainbow', weight: 8, points: 30 },
  BOMB: { name: 'bomb', weight: 12, points: -20 },
  FREEZE: { name: 'freeze', weight: 4, points: 15 },
  HEART: { name: 'heart', weight: 4, points: 10 },
  MULTIPLIER: { name: 'multiplier', weight: 5, points: 20 }
};

// Shop Items Definitions
const SHOP_BLADES = [
  { id: 'blade_classic', name: 'Classic Pointer', icon: '🖐️', cost: 0, currency: 'coins' },
  { id: 'blade_laser', name: 'Laser Sabre', icon: '⚔️', cost: 500, currency: 'coins', color: '#00f0ff' },
  { id: 'blade_flame', name: 'Flame Blade', icon: '🔥', cost: 1000, currency: 'coins', color: '#ff5722' },
  { id: 'blade_wand', name: 'Magic Wand', icon: '🪄', cost: 2000, currency: 'coins', color: '#9d4edd' },
  { id: 'blade_cyber', name: 'Cyber Pulse', icon: '⚡', cost: 30, currency: 'gems', color: '#06d6a0' }
];

const SHOP_THEMES = [
  { id: 'theme_neon', name: 'Neon Midnight', icon: '🌃', cost: 0, currency: 'coins', bg: 'linear-gradient(135deg, #0d0b1e 0%, #1a1638 50%, #0c1a30 100%)' },
  { id: 'theme_sunset', name: 'Solar Sunset', icon: '🌇', cost: 300, currency: 'coins', bg: 'linear-gradient(135deg, #2b0b1e 0%, #4a1525 50%, #1a0c30 100%)' },
  { id: 'theme_ocean', name: 'Deep Ocean', icon: '🌊', cost: 600, currency: 'coins', bg: 'linear-gradient(135deg, #041226 0%, #0a2540 50%, #051c2c 100%)' },
  { id: 'theme_galaxy', name: 'Cosmic Galaxy', icon: '🌌', cost: 1500, currency: 'coins', bg: 'linear-gradient(135deg, #12002b 0%, #28004d 50%, #08001a 100%)' }
];

// 50 Campaign Levels Generator
const CAMPAIGN_LEVELS = Array.from({ length: 50 }, (_, i) => {
  const lvl = i + 1;
  return {
    level: lvl,
    targetPops: 15 + lvl * 3,
    targetScore: 200 + lvl * 150,
    timeLimit: Math.max(30, 60 - Math.floor(lvl / 3)),
    maxBombs: Math.min(10, Math.floor(lvl / 2)),
    rewardCoins: 50 + lvl * 20,
    rewardGems: lvl % 5 === 0 ? 3 : 1
  };
});

class Game {
  constructor() {
    this.canvas = document.getElementById('gameCanvas');
    this.ctx = this.canvas.getContext('2d');

    // Economy & Persistence State
    this.coins = parseInt(localStorage.getItem('bp_coins') || '100', 10);
    this.gems = parseInt(localStorage.getItem('bp_gems') || '5', 10);
    this.unlockedBlades = JSON.parse(localStorage.getItem('bp_blades') || '["blade_classic"]');
    this.unlockedThemes = JSON.parse(localStorage.getItem('bp_themes') || '["theme_neon"]');
    this.equippedBlade = localStorage.getItem('bp_eq_blade') || 'blade_classic';
    this.equippedTheme = localStorage.getItem('bp_eq_theme') || 'theme_neon';
    this.completedLevels = JSON.parse(localStorage.getItem('bp_levels') || '{}');

    // UI Elements
    this.hud = document.getElementById('hud');
    this.coinsDisplay = document.getElementById('coins-display');
    this.gemsDisplay = document.getElementById('gems-display');
    this.scoreDisplay = document.getElementById('score-display');
    this.targetDisplay = document.getElementById('target-display');
    this.levelTargetCard = document.getElementById('level-target-card');
    this.comboContainer = document.getElementById('combo-box');
    this.comboText = document.getElementById('combo-multiplier');
    this.livesContainer = document.getElementById('lives-container');
    this.timerDisplay = document.getElementById('timer-display');
    this.modeStatLabel = document.getElementById('mode-stat-label');

    // Screens
    this.screens = {
      start: document.getElementById('start-screen'),
      campaign: document.getElementById('campaign-screen'),
      endless: document.getElementById('endless-screen'),
      shop: document.getElementById('shop-screen'),
      wheel: document.getElementById('wheel-screen'),
      quests: document.getElementById('quests-screen'),
      pause: document.getElementById('pause-screen'),
      levelclear: document.getElementById('levelclear-screen'),
      gameover: document.getElementById('gameover-screen')
    };

    // State Variables
    this.state = 'MENU'; // MENU, PLAYING, PAUSED, GAMEOVER, LEVELCLEAR
    this.gameMode = 'campaign'; // campaign, classic, time, arcade, zen
    this.currentLevel = 1;
    this.score = 0;
    this.lives = 3;
    this.timeLeft = 60;
    this.poppedCount = 0;
    this.maxCombo = 1;
    this.combo = 0;
    this.lastPopTime = 0;
    this.freezeTimer = 0;
    this.multiplierTimer = 0;

    // Entities & Physics
    this.balloons = [];
    this.particles = [];
    this.textPopups = [];
    this.swipeTrail = [];
    this.spawnTimer = 0;
    this.spawnInterval = 55;
    this.screenShake = 0;

    this.initResizing();
    this.initUI();
    this.updateCurrencyHUD();
    this.applyTheme(this.equippedTheme);

    // Game Loop Start
    requestAnimationFrame(this.loop.bind(this));
  }

  initResizing() {
    const resize = () => {
      this.width = window.innerWidth;
      this.height = window.innerHeight;
      this.canvas.width = this.width;
      this.canvas.height = this.height;
    };
    window.addEventListener('resize', resize);
    resize();
  }

  updateCurrencyHUD() {
    this.coinsDisplay.textContent = this.coins.toLocaleString();
    this.gemsDisplay.textContent = this.gems.toLocaleString();
    localStorage.setItem('bp_coins', this.coins.toString());
    localStorage.setItem('bp_gems', this.gems.toString());
  }

  applyTheme(themeId) {
    const theme = SHOP_THEMES.find(t => t.id === themeId);
    if (theme) {
      document.getElementById('app-container').style.background = theme.bg;
      this.equippedTheme = themeId;
      localStorage.setItem('bp_eq_theme', themeId);
    }
  }

  initUI() {
    // Top bar buttons
    document.getElementById('sound-btn').addEventListener('click', () => {
      const active = sounds.toggleSound();
      document.getElementById('sound-btn').textContent = active ? '🔊' : '🔇';
    });

    document.getElementById('shop-top-btn').addEventListener('click', () => {
      sounds.playClick();
      this.openShop();
    });

    // Home buttons
    document.getElementById('campaign-btn').addEventListener('click', () => {
      sounds.playClick();
      this.openCampaignMap();
    });

    document.getElementById('endless-btn').addEventListener('click', () => {
      sounds.playClick();
      this.showScreen('endless');
    });

    document.getElementById('wheel-btn').addEventListener('click', () => {
      sounds.playClick();
      this.showScreen('wheel');
    });

    document.getElementById('shop-btn').addEventListener('click', () => {
      sounds.playClick();
      this.openShop();
    });

    document.getElementById('quests-btn').addEventListener('click', () => {
      sounds.playClick();
      this.openQuests();
    });

    // Close modal triggers
    document.querySelectorAll('.close-modal-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        sounds.playClick();
        this.showScreen(e.currentTarget.dataset.target || 'start');
      });
    });

    // Endless mode selector buttons
    document.querySelectorAll('.mode-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        sounds.playClick();
        document.querySelectorAll('.mode-btn').forEach(b => b.classList.remove('active'));
        e.currentTarget.classList.add('active');
        this.selectedEndlessMode = e.currentTarget.dataset.mode;
      });
    });

    document.getElementById('start-endless-btn').addEventListener('click', () => {
      sounds.playClick();
      this.gameMode = this.selectedEndlessMode || 'classic';
      this.startGame();
    });

    // Pause / Resume / Game over buttons
    document.getElementById('pause-btn').addEventListener('click', () => {
      sounds.playClick();
      if (this.state === 'PLAYING') {
        this.state = 'PAUSED';
        this.screens.pause.classList.remove('hidden');
      }
    });

    document.getElementById('resume-btn').addEventListener('click', () => {
      sounds.playClick();
      if (this.state === 'PAUSED') {
        this.state = 'PLAYING';
        this.screens.pause.classList.add('hidden');
      }
    });

    document.getElementById('restart-btn').addEventListener('click', () => {
      sounds.playClick();
      this.startGame();
    });

    document.getElementById('quit-btn').addEventListener('click', () => {
      sounds.playClick();
      this.showScreen('start');
      this.hud.classList.add('hidden');
      this.state = 'MENU';
    });

    document.getElementById('next-level-btn').addEventListener('click', () => {
      sounds.playClick();
      this.currentLevel++;
      this.gameMode = 'campaign';
      this.startGame();
    });

    document.getElementById('stage-menu-btn').addEventListener('click', () => {
      sounds.playClick();
      this.openCampaignMap();
    });

    document.getElementById('play-again-btn').addEventListener('click', () => {
      sounds.playClick();
      this.startGame();
    });

    document.getElementById('over-menu-btn').addEventListener('click', () => {
      sounds.playClick();
      this.showScreen('start');
      this.hud.classList.add('hidden');
      this.state = 'MENU';
    });

    // Fortune Wheel Spin
    document.getElementById('spin-btn').addEventListener('click', () => {
      this.spinWheel();
    });

    // Input handlers for popping balloons
    const handlePopInput = (clientX, clientY) => {
      if (this.state !== 'PLAYING') return;

      const blade = SHOP_BLADES.find(b => b.id === this.equippedBlade) || SHOP_BLADES[0];
      this.swipeTrail.push({ x: clientX, y: clientY, alpha: 1.0, color: blade.color || '#00f0ff' });

      for (let i = this.balloons.length - 1; i >= 0; i--) {
        const b = this.balloons[i];
        if (b.popped) continue;

        const dist = Math.hypot(clientX - b.x, clientY - b.y);
        if (dist <= b.radius + 10) {
          this.popBalloon(b, i);
        }
      }
    };

    this.canvas.addEventListener('touchstart', (e) => {
      sounds.init();
      for (let i = 0; i < e.touches.length; i++) {
        handlePopInput(e.touches[i].clientX, e.touches[i].clientY);
      }
    });

    this.canvas.addEventListener('touchmove', (e) => {
      for (let i = 0; i < e.touches.length; i++) {
        handlePopInput(e.touches[i].clientX, e.touches[i].clientY);
      }
    });

    let isMouseDown = false;
    this.canvas.addEventListener('mousedown', (e) => {
      sounds.init();
      isMouseDown = true;
      handlePopInput(e.clientX, e.clientY);
    });

    this.canvas.addEventListener('mousemove', (e) => {
      if (isMouseDown) handlePopInput(e.clientX, e.clientY);
    });

    window.addEventListener('mouseup', () => { isMouseDown = false; });
  }

  showScreen(screenName) {
    Object.keys(this.screens).forEach(key => {
      if (key === screenName) {
        this.screens[key].classList.remove('hidden');
        this.screens[key].classList.add('active');
      } else {
        this.screens[key].classList.add('hidden');
        this.screens[key].classList.remove('active');
      }
    });
  }

  openCampaignMap() {
    const grid = document.getElementById('level-grid');
    grid.innerHTML = '';

    for (let i = 1; i <= 50; i++) {
      const card = document.createElement('div');
      const isUnlocked = i === 1 || this.completedLevels[i - 1];
      const stars = this.completedLevels[i] || 0;

      card.className = `level-card ${isUnlocked ? 'unlocked' : 'locked'}`;
      card.innerHTML = `
        <span class="level-num">${i}</span>
        <div class="level-stars">${isUnlocked ? '⭐'.repeat(stars) || '☆☆☆' : '🔒'}</div>
      `;

      if (isUnlocked) {
        card.addEventListener('click', () => {
          sounds.playClick();
          this.currentLevel = i;
          this.gameMode = 'campaign';
          this.startGame();
        });
      }
      grid.appendChild(card);
    }
    this.showScreen('campaign');
  }

  openShop() {
    this.renderShopItems('blades');
    document.querySelectorAll('.shop-tab').forEach(tab => {
      tab.addEventListener('click', (e) => {
        sounds.playClick();
        document.querySelectorAll('.shop-tab').forEach(t => t.classList.remove('active'));
        e.currentTarget.classList.add('active');
        this.renderShopItems(e.currentTarget.dataset.tab);
      });
    });
    this.showScreen('shop');
  }

  renderShopItems(tabType) {
    const container = document.getElementById('shop-items-container');
    container.innerHTML = '';

    const items = tabType === 'blades' ? SHOP_BLADES : SHOP_THEMES;
    const isUnlocked = (id) => tabType === 'blades' ? this.unlockedBlades.includes(id) : this.unlockedThemes.includes(id);
    const isEquipped = (id) => tabType === 'blades' ? this.equippedBlade === id : this.equippedTheme === id;

    items.forEach(item => {
      const card = document.createElement('div');
      card.className = `shop-item-card ${isEquipped(item.id) ? 'equipped' : ''}`;
      
      let btnLabel = isEquipped(item.id) ? 'EQUIPPED' : isUnlocked(item.id) ? 'EQUIP' : `${item.cost} ${item.currency === 'coins' ? '🪙' : '💎'}`;

      card.innerHTML = `
        <div class="item-icon">${item.icon}</div>
        <div class="item-title">${item.name}</div>
        <button class="buy-btn">${btnLabel}</button>
      `;

      card.querySelector('.buy-btn').addEventListener('click', () => {
        sounds.playClick();
        if (isUnlocked(item.id)) {
          if (tabType === 'blades') this.equippedBlade = item.id;
          else this.applyTheme(item.id);
        } else {
          if (item.currency === 'coins' && this.coins >= item.cost) {
            this.coins -= item.cost;
            if (tabType === 'blades') this.unlockedBlades.push(item.id);
            else this.unlockedThemes.push(item.id);
            this.updateCurrencyHUD();
          } else if (item.currency === 'gems' && this.gems >= item.cost) {
            this.gems -= item.cost;
            if (tabType === 'blades') this.unlockedBlades.push(item.id);
            else this.unlockedThemes.push(item.id);
            this.updateCurrencyHUD();
          }
        }
        localStorage.setItem('bp_blades', JSON.stringify(this.unlockedBlades));
        localStorage.setItem('bp_themes', JSON.stringify(this.unlockedThemes));
        this.renderShopItems(tabType);
      });

      container.appendChild(card);
    });
  }

  spinWheel() {
    const wheel = document.getElementById('fortune-wheel');
    const spinBtn = document.getElementById('spin-btn');
    spinBtn.disabled = true;

    const randomDeg = 1440 + Math.floor(Math.random() * 360);
    wheel.style.transform = `rotate(${randomDeg}deg)`;
    sounds.playWheelTick();

    setTimeout(() => {
      sounds.playCoin();
      this.coins += 500;
      this.updateCurrencyHUD();
      spinBtn.disabled = false;
      alert('🎉 You won 500 Coins!');
    }, 4100);
  }

  openQuests() {
    const list = document.getElementById('quests-list');
    list.innerHTML = `
      <div class="quest-card">
        <div class="quest-info">
          <div class="quest-title">Pop 50 Balloons</div>
          <div class="quest-progress">Progress: 50 / 50</div>
        </div>
        <button class="buy-btn">CLAIM 100 🪙</button>
      </div>
      <div class="quest-card">
        <div class="quest-info">
          <div class="quest-title">Clear Stage 5</div>
          <div class="quest-progress">Progress: ${this.completedLevels[5] ? 'Completed' : 'Incomplete'}</div>
        </div>
        <button class="buy-btn">CLAIM 5 💎</button>
      </div>
    `;
    this.showScreen('quests');
  }

  startGame() {
    this.state = 'PLAYING';
    this.score = 0;
    this.lives = 3;
    this.poppedCount = 0;
    this.maxCombo = 1;
    this.combo = 0;
    this.balloons = [];
    this.particles = [];
    this.textPopups = [];

    this.hud.classList.remove('hidden');
    this.showScreen(null);

    if (this.gameMode === 'campaign') {
      const lvlConfig = CAMPAIGN_LEVELS[this.currentLevel - 1];
      this.targetPops = lvlConfig.targetPops;
      this.timeLeft = lvlConfig.timeLimit;
      this.levelTargetCard.classList.remove('hidden');
      this.targetDisplay.textContent = `Pop ${this.targetPops}`;
      this.modeStatLabel.textContent = 'TIME';
      this.livesContainer.classList.add('hidden');
      this.timerDisplay.classList.remove('hidden');
      this.timerDisplay.textContent = `${this.timeLeft}s`;
      this.startTimer();
    } else {
      this.levelTargetCard.classList.add('hidden');
      if (this.gameMode === 'time') {
        this.timeLeft = 60;
        this.modeStatLabel.textContent = 'TIME';
        this.livesContainer.classList.add('hidden');
        this.timerDisplay.classList.remove('hidden');
        this.startTimer();
      } else {
        this.modeStatLabel.textContent = 'LIVES';
        this.livesContainer.classList.remove('hidden');
        this.timerDisplay.classList.add('hidden');
        this.updateLivesHUD();
      }
    }
  }

  startTimer() {
    if (this.timerInterval) clearInterval(this.timerInterval);
    this.timerInterval = setInterval(() => {
      if (this.state === 'PLAYING') {
        this.timeLeft--;
        this.timerDisplay.textContent = `${this.timeLeft}s`;
        if (this.timeLeft <= 0) {
          clearInterval(this.timerInterval);
          this.checkGameCompletion();
        }
      }
    }, 1000);
  }

  updateLivesHUD() {
    let hearts = '';
    for (let i = 0; i < this.lives; i++) hearts += '<span class="heart">❤️</span>';
    this.livesContainer.innerHTML = hearts;
  }

  popBalloon(balloon, index) {
    if (balloon.popped) return;
    balloon.popped = true;

    if (navigator.vibrate) navigator.vibrate(20);

    const now = performance.now();
    if (now - this.lastPopTime < 800) this.combo++;
    else this.combo = 1;
    this.lastPopTime = now;

    if (this.combo > this.maxCombo) this.maxCombo = this.combo;

    if (this.combo >= 2) {
      this.comboContainer.classList.remove('hidden');
      this.comboText.textContent = `${this.combo}x`;
      sounds.playCombo(this.combo);
    }

    if (balloon.type === 'bomb') {
      sounds.playBomb();
      this.screenShake = 15;
      if (this.gameMode !== 'zen') {
        this.lives--;
        this.updateLivesHUD();
        if (this.lives <= 0) this.gameOver();
      }
    } else {
      sounds.playPop(balloon.radius);
      this.score += BALLOON_TYPES[balloon.type.toUpperCase()].points * Math.min(this.combo, 5);
      this.poppedCount++;
      this.coins += 2;
      this.updateCurrencyHUD();
      this.scoreDisplay.textContent = this.score.toLocaleString();
    }

    // Particle Burst
    for (let i = 0; i < 14; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = 2 + Math.random() * 5;
      this.particles.push({
        x: balloon.x,
        y: balloon.y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed,
        radius: 3,
        color: balloon.color,
        alpha: 1.0
      });
    }

    if (this.gameMode === 'campaign' && this.poppedCount >= this.targetPops) {
      this.stageClear();
    }
  }

  spawnBalloon() {
    const radius = 28 + Math.random() * 18;
    const x = radius + Math.random() * (this.width - radius * 2);
    const y = this.height + radius + 10;
    const types = Object.keys(BALLOON_TYPES);
    const typeKey = types[Math.floor(Math.random() * types.length)];
    const colors = ['#ff2a75', '#00f0ff', '#9d4edd', '#ffb703', '#06d6a0'];

    this.balloons.push({
      x,
      y,
      radius,
      vy: 2.2 + Math.random() * 1.8,
      type: BALLOON_TYPES[typeKey].name,
      color: colors[Math.floor(Math.random() * colors.length)],
      wobblePhase: Math.random() * Math.PI * 2,
      popped: false
    });
  }

  checkGameCompletion() {
    if (this.gameMode === 'campaign') {
      if (this.poppedCount >= this.targetPops) this.stageClear();
      else this.gameOver();
    } else {
      this.gameOver();
    }
  }

  stageClear() {
    this.state = 'LEVELCLEAR';
    sounds.playVictory();
    if (this.timerInterval) clearInterval(this.timerInterval);

    this.completedLevels[this.currentLevel] = 3;
    localStorage.setItem('bp_levels', JSON.stringify(this.completedLevels));

    this.coins += 150;
    this.gems += 2;
    this.updateCurrencyHUD();

    document.getElementById('stage-clear-title').textContent = `STAGE ${this.currentLevel} CLEAR!`;
    document.getElementById('earned-coins').textContent = '150';
    document.getElementById('earned-gems').textContent = '2';

    this.showScreen('levelclear');
  }

  gameOver() {
    this.state = 'GAMEOVER';
    if (this.timerInterval) clearInterval(this.timerInterval);

    document.getElementById('final-score').textContent = this.score.toLocaleString();
    document.getElementById('final-popped').textContent = this.poppedCount.toLocaleString();
    document.getElementById('final-combo').textContent = `${this.maxCombo}x`;
    document.getElementById('over-coins').textContent = `${Math.floor(this.score / 10)}`;

    this.showScreen('gameover');
  }

  update() {
    if (this.state !== 'PLAYING') return;

    this.spawnTimer++;
    if (this.spawnTimer >= this.spawnInterval) {
      this.spawnTimer = 0;
      this.spawnBalloon();
    }

    for (let i = this.balloons.length - 1; i >= 0; i--) {
      const b = this.balloons[i];
      if (b.popped) {
        this.balloons.splice(i, 1);
        continue;
      }
      b.y -= b.vy;
      b.wobblePhase += 0.04;
      b.x += Math.sin(b.wobblePhase) * 0.8;

      if (b.y + b.radius < -20) {
        this.balloons.splice(i, 1);
      }
    }

    for (let i = this.particles.length - 1; i >= 0; i--) {
      const p = this.particles[i];
      p.x += p.vx;
      p.y += p.vy;
      p.alpha -= 0.04;
      if (p.alpha <= 0) this.particles.splice(i, 1);
    }

    for (let i = this.swipeTrail.length - 1; i >= 0; i--) {
      const t = this.swipeTrail[i];
      t.alpha -= 0.08;
      if (t.alpha <= 0) this.swipeTrail.splice(i, 1);
    }

    if (this.screenShake > 0) this.screenShake *= 0.85;
  }

  draw() {
    this.ctx.clearRect(0, 0, this.width, this.height);

    this.ctx.save();
    if (this.screenShake > 0.5) {
      this.ctx.translate((Math.random() - 0.5) * this.screenShake, (Math.random() - 0.5) * this.screenShake);
    }

    // Draw Balloons
    for (let b of this.balloons) {
      this.ctx.save();
      this.ctx.beginPath();
      this.ctx.arc(b.x, b.y, b.radius, 0, Math.PI * 2);
      this.ctx.fillStyle = b.color;
      this.ctx.shadowColor = 'rgba(0, 0, 0, 0.4)';
      this.ctx.shadowBlur = 10;
      this.ctx.fill();

      // Specular Dot
      this.ctx.beginPath();
      this.ctx.arc(b.x - b.radius * 0.3, b.y - b.radius * 0.3, b.radius * 0.2, 0, Math.PI * 2);
      this.ctx.fillStyle = 'rgba(255, 255, 255, 0.6)';
      this.ctx.fill();

      if (b.type === 'bomb') {
        this.ctx.font = `${b.radius}px sans-serif`;
        this.ctx.textAlign = 'center';
        this.ctx.textBaseline = 'middle';
        this.ctx.fillText('💣', b.x, b.y);
      }
      this.ctx.restore();
    }

    // Draw Particles
    for (let p of this.particles) {
      this.ctx.save();
      this.ctx.globalAlpha = p.alpha;
      this.ctx.fillStyle = p.color;
      this.ctx.beginPath();
      this.ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
      this.ctx.fill();
      this.ctx.restore();
    }

    // Draw Blade Swipe Trail
    if (this.swipeTrail.length > 1) {
      this.ctx.save();
      for (let i = 1; i < this.swipeTrail.length; i++) {
        const p1 = this.swipeTrail[i - 1];
        const p2 = this.swipeTrail[i];
        this.ctx.strokeStyle = p2.color || '#00f0ff';
        this.ctx.globalAlpha = p2.alpha;
        this.ctx.lineWidth = 6 * p2.alpha;
        this.ctx.lineCap = 'round';
        this.ctx.beginPath();
        this.ctx.moveTo(p1.x, p1.y);
        this.ctx.lineTo(p2.x, p2.y);
        this.ctx.stroke();
      }
      this.ctx.restore();
    }

    this.ctx.restore();
  }

  loop() {
    this.update();
    this.draw();
    requestAnimationFrame(this.loop.bind(this));
  }
}

window.addEventListener('DOMContentLoaded', () => {
  window.game = new Game();
});
