package context;

public class MJType {
    private String name;
    enum Type {
        ARRAY, BOOLEAN, INT, IDENT, OTHER;

        public static Type fromInteger(int x) {
            switch (x) {
                case 0:
                    return ARRAY;
                case 1:
                    return BOOLEAN;
                case 2:
                    return INT;
                case 3:
                    return IDENT;
                default:
                    return OTHER;
            }
        }
    }
    private Type type;

    public MJType(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if ((o instanceof MJType)) {
            if (((MJType) o).type == Type.IDENT) {
                return this.name.equals(((MJType) o).name);
            }
            return ((MJType) o).type == this.type;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 17 + name.hashCode();
        return prime * result + ((this.type == null) ? 0 : this.type.hashCode());
    }
}
