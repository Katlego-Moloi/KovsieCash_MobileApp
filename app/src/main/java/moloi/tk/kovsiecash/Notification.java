package moloi.tk.kovsiecash;


public class Notification {
    public Integer NotificationID;
    public String Type;
    public String NotificationDescription;
    public String NotificationDateTime;
    public Integer Status;
    public Integer UserID;

    public Notification(Integer _notificationID, String _type, String _notificationDescription,
                        String _notificationDateTime, Integer _status, Integer _userID) {
        this.NotificationID = _notificationID;
        this.Type = _type;
        this.NotificationDescription = _notificationDescription;
        this.NotificationDateTime = _notificationDateTime;
        this.Status = _status;
        this.UserID = _userID;
    }

    // Getters and Setters
    public Integer getNotificationID() {
        return NotificationID;
    }

    public void setNotificationID(Integer notificationID) {
        NotificationID = notificationID;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getNotificationDescription() {
        return NotificationDescription;
    }

    public void setNotificationDescription(String notificationDescription) {
        NotificationDescription = notificationDescription;
    }

    public String getNotificationDateTime() {
        return NotificationDateTime;
    }

    public void setNotificationDateTime(String notificationDateTime) {
        NotificationDateTime = notificationDateTime;
    }

    public Integer getStatus() {
        return Status;
    }

    public void setStatus(Integer status) {
        Status = status;
    }

    public Integer getUserID() {
        return UserID;
    }

    public void setUserID(Integer userID) {
        UserID = userID;
    }
}