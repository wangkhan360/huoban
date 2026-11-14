import React, { useState } from 'react';
import { CompanionState, GradeBand } from '../types/student';
import { COMPANION_STATE_META } from '../data/mockStudentData';
import yunlongIdleImg from '../assets/images/yunlong_hero_companion_1794558246337.jpg';
import yunlongListeningImg from '../assets/images/yunlong_listening_pose_1794558247042.jpg';
import yunlongCelebratingImg from '../assets/images/yunlong_celebrating_pose_1794558248617.jpg';

interface YunlongStageProps {
  state: CompanionState;
  companionName: string;
  gradeBand: GradeBand;
  size?: 'sm' | 'md' | 'lg';
  floating?: boolean;
  isSpeakingTts?: boolean;
  onInterruptTts?: () => void;
  statusLabelOverride?: string;
  hideMotionHint?: boolean;
}

export const YunlongStage: React.FC<YunlongStageProps> = ({
  state,
  companionName,
  gradeBand,
  size = 'md',
  floating = false,
  isSpeakingTts = false,
  onInterruptTts,
  statusLabelOverride,
  hideMotionHint = false,
}) => {
  const [imgFailed, setImgFailed] = useState(false);
  const meta = COMPANION_STATE_META[state];
  const isLower = gradeBand === 'LOWER_GRADE';

  const imgSrc =
    state === 'listening'
      ? yunlongListeningImg
      : state === 'celebrating' || state === 'happy'
      ? yunlongCelebratingImg
      : yunlongIdleImg;

  const animClass =
    state === 'listening'
      ? 'animate-yunlong-listen'
      : state === 'speaking'
      ? 'animate-yunlong-speak'
      : state === 'celebrating' || state === 'happy'
      ? 'animate-yunlong-celebrate'
      : state === 'resting'
      ? 'opacity-85 animate-yunlong-breathe'
      : 'animate-yunlong-breathe';

  const boxDims =
    size === 'lg' ? 'w-48 h-48' : size === 'sm' ? 'w-24 h-24' : 'w-32 h-32';

  const haloBorder =
    state === 'listening'
      ? 'ring-4 ring-[#2578FF]/30'
      : state === 'celebrating' || state === 'happy'
      ? 'ring-4 ring-[#10A37F]/30'
      : state === 'thinking'
      ? 'ring-4 ring-[#4FB8FF]/35'
      : state === 'resting'
      ? 'ring-2 ring-[#7B8FA8]/25'
      : 'ring-2 ring-white/80';

  const statusText = statusLabelOverride || (isLower ? meta.lowerLabel : meta.upperLabel);
  const showHint = !floating && !hideMotionHint && !statusLabelOverride;

  return (
    <div className="flex flex-col items-center select-none">
      <div className="relative flex items-center justify-center">
        {floating && (
          <div
            aria-hidden="true"
            className="absolute -inset-3 rounded-full bg-radial from-[#4FB8FF]/22 via-[#DCEBFF]/15 to-transparent blur-md pointer-events-none"
          />
        )}
        <div
          className={`relative ${boxDims} rounded-full overflow-hidden ${
            floating
              ? 'bg-white/80 backdrop-blur-md shadow-[0_12px_32px_-8px_rgba(37,120,255,0.28)]'
              : 'bg-gradient-to-b from-[#EAF3FF] to-[#F9FCFF] shadow-sm'
          } ${haloBorder} flex items-center justify-center transition-all duration-200 ${animClass}`}
        >
          {!imgFailed ? (
            <img
              src={imgSrc}
              alt={`${companionName} - ${meta.title}`}
              referrerPolicy="no-referrer"
              onError={() => setImgFailed(true)}
              className="w-full h-full object-cover mix-blend-multiply scale-105"
            />
          ) : (
            <div className="w-full h-full flex flex-col items-center justify-center bg-[#EAF3FF] text-[#2578FF] p-3 text-center">
              <span className="text-sm font-semibold">{companionName}</span>
              <span className="text-xs text-[#475C78] mt-1">{state}</span>
            </div>
          )}
        </div>

        {isSpeakingTts && onInterruptTts && (
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              onInterruptTts();
            }}
            className="absolute -bottom-1 -right-6 min-h-[34px] px-3 py-1 rounded-full bg-white/95 border border-[#E1EAF4] text-xs font-medium text-[#13233A] shadow-sm hover:bg-[#EEF4FB] transition-colors whitespace-nowrap"
          >
            打断
          </button>
        )}
      </div>

      <p
        className={`mt-1.5 text-xs font-medium px-3 py-0.5 rounded-full ${
          floating
            ? 'bg-white/85 backdrop-blur-sm text-[#475C78] border border-[#E1EAF4]/80 shadow-xs'
            : 'text-[#475C78]'
        }`}
      >
        {statusText}
        {showHint && (
          <>
            <span className="mx-1.5 text-[#7B8FA8]" aria-hidden="true">
              ·
            </span>
            <span className="text-[#7B8FA8]">{meta.motionHint}</span>
          </>
        )}
      </p>
    </div>
  );
};
