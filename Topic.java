package es.glaux.model;

/** Represents a topic belonging to a subject. */
public class Topic {
    private final int id;
    private final int subjectId;
    private final String subjectName;
    private final String name;
    private final String description;

    public Topic(int id, int subjectId, String subjectName, String name, String description) {
        this.id = id;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.name = name;
        this.description = description;
    }

    public int getId() { return id; }
    public int getSubjectId() { return subjectId; }
    public String getSubjectName() { return subjectName; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    @Override
    public String toString() { return subjectName + " — " + name; }
}
