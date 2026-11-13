import React, { useState } from 'react';
import yunlongIdleImg from '../assets/images/yunlong_hero_companion_1794558246337.jpg';
import yunlongListeningImg from '../assets/images/yunlong_listening_pose_1794558247042.jpg';
import yunlongCelebratingImg from '../assets/images/yunlong_celebrating_pose_1794558248617.jpg';
import yunlongAppIconImg from '../assets/images/yunlong_app_icon_1794558247476.jpg';
import rewardScienceKitImg from '../assets/images/reward_science_kit_1794558247850.jpg';

const COLOR_TOKENS = [
  { name: 'Canvas Ice（主画布）', hex: '#F5F8FC', role: '60% 背景浅色渐变基底' },
  { name: 'Surface Card（卡片面）', hex: '#FFFFFF', role: '30% 结构卡片与 AI 气泡' },
  { name: 'Tech Blue Primary（科技主蓝）', hex: '#2578FF', role: '10% 语音主按钮与孩子气泡' },
  { name: 'Sky Cyan Glow（云龙光晕蓝）', hex: '#4FB8FF', role: '云龙动态光环与呼吸反馈' },
  { name: 'Ink Primary（深海墨色）', hex: '#13233A', role: '正文与标题高对比文字' },
  { name: 'Success Mint（成长达成绿）', hex: '#10A37F', role: '完成反馈与家长已确认' },
  { name: 'Warning Amber（草案提醒橙）', hex: '#D9820B', role: '未批准草案与权限提示' },
  { name: 'Danger Coral（警示珊瑚红）', hex: '#E04848', role: '停止录音与时间冲突提示' },
];

const CUSTOM_ICONS = [
  {
    name: 'ic_yunlong_mic',
    label: '语音对话主麦',
    androidPath: 'res/drawable/ic_yunlong_mic.xml',
    svg: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6">
        <path d="M12 3a3 3 0 0 0-3 3v5a3 3 0 0 0 6 0V6a3 3 0 0 0-3-3Z" />
        <path d="M19 10v1a7 7 0 0 1-14 0v-1M12 18v3M8.5 21h7" />
      </svg>
    ),
  },
  {
    name: 'ic_yunlong_points',
    label: '成长积分星标',
    androidPath: 'res/drawable/ic_yunlong_points.xml',
    svg: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6">
        <path d="m12 3 2.6 5.3 5.9.8-4.3 4.2 1 5.8-5.2-2.7-5.2 2.7 1-5.8-4.3-4.2 5.9-.8L12 3Z" />
      </svg>
    ),
  },
  {
    name: 'ic_yunlong_task',
    label: '今日任务清单',
    androidPath: 'res/drawable/ic_yunlong_task.xml',
    svg: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6">
        <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2M9 5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2M9 5a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2M9 12l2 2 4-4" />
      </svg>
    ),
  },
  {
    name: 'ic_yunlong_schedule',
    label: '时间表与规划',
    androidPath: 'res/drawable/ic_yunlong_schedule.xml',
    svg: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6">
        <path d="M12 7v5l3.5 2M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
      </svg>
    ),
  },
  {
    name: 'ic_yunlong_goal',
    label: '成长目标靶心',
    androidPath: 'res/drawable/ic_yunlong_goal.xml',
    svg: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6">
        <circle cx="12" cy="12" r="9" />
        <circle cx="12" cy="12" r="4.5" />
        <circle cx="12" cy="12" r="1.5" />
      </svg>
    ),
  },
  {
    name: 'ic_yunlong_wish',
    label: '愿望家庭协商',
    androidPath: 'res/drawable/ic_yunlong_wish.xml',
    svg: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6">
        <path d="M12 20.5S4 15.5 4 9.5A4.8 4.8 0 0 1 9 4.5c1.55 0 2.94.71 3.86 1.82A4.9 4.9 0 0 1 16.72 4.5 4.9 4.9 0 0 1 21.72 9.5C21.72 15.5 12 20.5 12 20.5Z" />
      </svg>
    ),
  },
  {
    name: 'ic_yunlong_reading',
    label: '英语朗读课本',
    androidPath: 'res/drawable/ic_yunlong_reading.xml',
    svg: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6">
        <path d="M4 19.5v-14a2 2 0 0 1 2-2h13v17H6a2 2 0 0 1-2-2Zm0 0a2 2 0 0 1 2-2h13M8 8h7M8 11.5h5" />
      </svg>
    ),
  },
];

export const DesignSystemShowcase: React.FC = () => {
  const [copiedName, setCopiedName] = useState<string | null>(null);

  return (
    <div className="max-w-6xl mx-auto px-6 py-8 space-y-10">
      <section className="space-y-2">
        <h2 className="text-2xl font-bold text-[#13233A]">设计系统规范与配套视觉素材库</h2>
        <p className="text-sm text-[#475C78] max-w-3xl">
          所有 Color Tokens、Typography、Spacing、Radius、Icon Style 与云龙插画素材已同步写入 Android 工程
          <code className="mx-1 px-1.5 py-0.5 bg-[#EEF4FB] rounded text-xs font-mono-tabular">
            ui/theme/DesignTokens.kt
          </code>
          与
          <code className="mx-1 px-1.5 py-0.5 bg-[#EEF4FB] rounded text-xs font-mono-tabular">
            res/drawable/
          </code>
          ，兼顾一年级清晰易读与六年级不幼稚的商业级科技蓝审美。
        </p>
      </section>

      {/* Generated Designer Visual Assets */}
      <section className="space-y-4">
        <h3 className="text-lg font-semibold text-[#13233A]">01. 云龙视觉主角与商业级图片素材</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
          {[
            { src: yunlongAppIconImg, title: 'App 启动图标', file: 'img_yunlong_app_icon.jpg', ratio: '1:1 · 自适应前景另附 XML' },
            { src: yunlongIdleImg, title: '云龙主态（待机/思考/说话）', file: 'img_yunlong_idle.jpg', ratio: '1:1 · 配合 Compose 呼吸节奏' },
            { src: yunlongListeningImg, title: '云龙倾听态（轻微歪头）', file: 'img_yunlong_listening.jpg', ratio: '1:1 · 声波光晕过渡' },
            { src: yunlongCelebratingImg, title: '云龙庆祝/开心态', file: 'img_yunlong_celebrating.jpg', ratio: '1:1 · 星光激励动效' },
            { src: rewardScienceKitImg, title: '实物奖励图（天文实验盒）', file: 'img_reward_science_kit.jpg', ratio: '4:3 · 奖励商店商品卡' },
          ].map((asset) => (
            <div key={asset.file} className="bg-white border border-[#E1EAF4] rounded-2xl overflow-hidden flex flex-col">
              <div className="aspect-square bg-[#F5F8FC] overflow-hidden">
                <img
                  src={asset.src}
                  alt={asset.title}
                  referrerPolicy="no-referrer"
                  className="w-full h-full object-cover"
                />
              </div>
              <div className="p-3 space-y-1">
                <p className="text-sm font-semibold text-[#13233A]">{asset.title}</p>
                <p className="text-xs text-[#7B8FA8] font-mono-tabular truncate">{asset.file}</p>
                <p className="text-xs text-[#475C78]">{asset.ratio}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Color Tokens */}
      <section className="space-y-4">
        <h3 className="text-lg font-semibold text-[#13233A]">02. Color Tokens（60-30-10 科技天空蓝调色板）</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {COLOR_TOKENS.map((c) => (
            <div key={c.hex} className="bg-white border border-[#E1EAF4] rounded-2xl p-3 flex items-center gap-3">
              <div
                className="w-12 h-12 rounded-xl border border-[#E1EAF4] shrink-0"
                style={{ backgroundColor: c.hex }}
              />
              <div className="min-w-0">
                <p className="text-sm font-medium text-[#13233A] truncate">{c.name}</p>
                <p className="text-xs text-[#2578FF] font-mono-tabular">{c.hex}</p>
                <p className="text-xs text-[#7B8FA8] truncate">{c.role}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Custom Vector Icons */}
      <section className="space-y-4">
        <h3 className="text-lg font-semibold text-[#13233A]">03. 统一线条 Icon Style（1.9dp 圆润描边矢量图标）</h3>
        <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-3">
          {CUSTOM_ICONS.map((ic) => (
            <button
              key={ic.name}
              type="button"
              onClick={() => setCopiedName(ic.androidPath)}
              className="bg-white border border-[#E1EAF4] rounded-2xl p-4 flex flex-col items-center text-center gap-2 hover:border-[#2578FF] transition-colors"
            >
              <span className="text-[#13233A]">{ic.svg}</span>
              <span className="text-xs font-medium text-[#13233A]">{ic.label}</span>
              <span className="text-[11px] text-[#7B8FA8] font-mono-tabular truncate w-full">{ic.name}</span>
            </button>
          ))}
        </div>
        {copiedName && (
          <p className="text-xs text-[#2578FF]">
            已定位 Android Vector Drawable 资源路径：<span className="font-mono-tabular">android-app/app/src/main/{copiedName}</span>
          </p>
        )}
      </section>

      {/* Geometry & Rhythm */}
      <section className="space-y-4">
        <h3 className="text-lg font-semibold text-[#13233A]">04. 间距、圆角、触控尺寸与动效时长</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-white border border-[#E1EAF4] rounded-2xl p-4 space-y-1.5">
            <p className="text-sm font-semibold text-[#13233A]">Spacing & Radius</p>
            <p className="text-xs text-[#475C78]">基准间距：4 / 8 / 12 / 16 / 20 / 24 dp</p>
            <p className="text-xs text-[#475C78]">卡片圆角 20dp，按钮圆角 14dp，嵌套内距严格保持外大内小</p>
          </div>
          <div className="bg-white border border-[#E1EAF4] rounded-2xl p-4 space-y-1.5">
            <p className="text-sm font-semibold text-[#13233A]">儿童友好触控热区</p>
            <p className="text-xs text-[#475C78]">主按钮高度 52dp，紧凑按钮 44dp，图标热区最小 48×48dp</p>
            <p className="text-xs text-[#475C78]">底部语音主入口位于拇指自然触达区</p>
          </div>
          <div className="bg-white border border-[#E1EAF4] rounded-2xl p-4 space-y-1.5">
            <p className="text-sm font-semibold text-[#13233A]">Animation Duration</p>
            <p className="text-xs text-[#475C78]">快速反馈 180ms · 状态切换 320ms · 呼吸周期 3400ms</p>
            <p className="text-xs text-[#475C78]">支持系统减弱动态效果及说话随时打断</p>
          </div>
        </div>
      </section>
    </div>
  );
};
