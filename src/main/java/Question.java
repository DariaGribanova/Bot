import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Question {
    private String name;
    private String item;
    private boolean isCompleted;
    private String id;
    private String page;

    private List<Answer> answers = new ArrayList<>();

    public Question(String name, String item, boolean isCompleted, String id, String page, List<Answer> answers) {
        this.name = name;
        this.item = item;
        this.isCompleted = isCompleted;
        this.id = id;
        this.page = page;
        this.answers = answers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Question() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }
    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return "Question{" +
                "name='" + name + '\'' +
                ", item='" + item + '\'' +
                ", isCompleted=" + isCompleted +
                ", id='" + id + '\'' +
                ", page='" + page + '\'' +
                ", answers=" + answers +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return isCompleted == question.isCompleted && Objects.equals(name, question.name) && Objects.equals(item, question.item) && Objects.equals(id, question.id) && Objects.equals(page, question.page) && Objects.equals(answers, question.answers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, item, isCompleted, id, page, answers);
    }
}
