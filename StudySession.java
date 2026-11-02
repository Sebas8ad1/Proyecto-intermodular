package es.glaux.model;

/** Represents a study session recorded by the student. */
public class StudySession {
    private final int id;
    private final int topicId;
    private final String topicName;
    private final String subjectName;
    private final String date;
    private final int minutes;
    private final String notes;

    public StudySession(int id, int topicId, String topicName, String subjectName,
                        String date, int minutes, String notes) {
        this.id = id;
        this.topicId = topicId;
        this.topicName = topicName;
        this.subjectName = subjectName;
        this.date = date;
        this.minutes = minutes;
        this.notes = notes;
    }

    public int getId() { return id; }
    public int getTopicId() { return topicId; }
    public String getTopicName() { return topicName; }
    public String getSubjectName() { return subjectName; }
    public String getDate() { return date; }
    public int getMinutes() { return minutes; }
    public String getNotes() { return notes; }
}
