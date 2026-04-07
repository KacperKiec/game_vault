CREATE OR REPLACE FUNCTION func_notify_review()
RETURNS TRIGGER AS $$
BEGIN
INSERT INTO notifications (
    recipient_id,
    type,
    title,
    content,
    metadata,
    created_at,
    read,
    ws_sent
)
SELECT
    user_id,
    'NEW_REVIEW',
    'New review',
    'Someone added new review to the game you''re interested in.',
    jsonb_build_object('guid', NEW.game_id),
    NOW(),
    false,
    false
FROM game_list
WHERE game_id = NEW.game_id AND user_id != NEW.user_id;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_after_review_insert
    AFTER INSERT ON reviews
    FOR EACH ROW
    EXECUTE FUNCTION func_notify_review();