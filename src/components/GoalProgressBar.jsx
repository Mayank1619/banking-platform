/**
 * Goal Progress Bar Component
 *
 * Displays a visual progress bar for savings goal progress
 * - Progress percentage capped at 100%
 * - Shows percentage text
 * - Supports both Classic and Neon themes
 */

import React from "react";
import PropTypes from "prop-types";
import "../styles.css";

const GoalProgressBar = ({
  progressPercentage = 0,
  currentBalance = 0,
  targetAmount = 0,
  theme = "classic",
}) => {
  // Cap progress at 100%
  const displayProgress = Math.min(progressPercentage, 100);

  // Determine progress color based on percentage
  const getProgressColor = () => {
    if (displayProgress === 100) return "var(--color-success, #28a745)";
    if (displayProgress >= 75) return "var(--color-warning, #ffc107)";
    if (displayProgress >= 50) return "var(--color-info, #17a2b8)";
    return "var(--color-primary, #007bff)";
  };

  const getThemeClass = () => {
    return theme === "neon" ? "progress-bar-neon" : "progress-bar-classic";
  };

  return (
    <div className={`goal-progress-container ${getThemeClass()}`}>
      <div className="progress-info">
        <span className="progress-label">Progress</span>
        <span className="progress-percentage">
          {Math.round(displayProgress)}%
        </span>
      </div>

      <div className="progress-bar-wrapper">
        <div
          className="progress-bar-track"
          role="progressbar"
          aria-valuenow={displayProgress}
          aria-valuemin="0"
          aria-valuemax="100"
        >
          <div
            className="progress-bar-fill"
            style={{
              width: `${displayProgress}%`,
              backgroundColor: getProgressColor(),
            }}
          />
        </div>
      </div>

      <div className="progress-details">
        <span className="balance-info">
          Saved:{" "}
          <span className="balance-amount">${currentBalance.toFixed(2)}</span>
        </span>
        <span className="target-info">of ${targetAmount.toFixed(2)}</span>
      </div>
    </div>
  );
};

GoalProgressBar.propTypes = {
  progressPercentage: PropTypes.number,
  currentBalance: PropTypes.number,
  targetAmount: PropTypes.number,
  theme: PropTypes.oneOf(["classic", "neon"]),
};

export default GoalProgressBar;
