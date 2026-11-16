// A coloured banner that tells the user what just happened.
// type is "success" or "error".
export default function FeedbackMessage({ message, onClose }) {
  if (!message) return null;
  return (
    <div
      className={`feedback feedback-${message.type}`}
      role={message.type === 'error' ? 'alert' : 'status'}
    >
      <span>{message.text}</span>
      <button type="button" className="link-button" onClick={onClose} aria-label="Dismiss message">
        ✕
      </button>
    </div>
  );
}
