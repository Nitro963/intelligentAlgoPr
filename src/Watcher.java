import java.io.Serializable;
import java.util.Objects;

abstract public class Watcher implements Cloneable, Serializable {
    protected String name;
    protected Constrain constrain;
    protected int cntMax;
    protected String type;
    protected int id;

    public Watcher(String name, int id, Constrain constrain, int cntMax) {
        this.name = name;
        this.constrain = constrain;
        this.cntMax = cntMax;
        this.type = null;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Constrain getConstrain() {
        return constrain;
    }

    public int getCntMax() {
        return cntMax;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Watcher watcher = (Watcher) o;
        return name.equals(watcher.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    protected Object clone() {
        try {
            Watcher w = (Watcher) super.clone();
            w.name = this.name;
            w.cntMax = cntMax;
            if (constrain != null)
                w.constrain = (Constrain) this.constrain.clone();
            w.type = type;
            w.id = id;
            return w;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Watcher{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
