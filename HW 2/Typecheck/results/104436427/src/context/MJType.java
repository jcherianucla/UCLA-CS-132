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
    private String subtype = null;

    public MJType(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public MJType(String name, Type type, String subtype) {
        this.name = name;
        this.type = type;
        if (type == Type.IDENT)
            this.subtype = subtype;
    }

    public boolean hasSubtype() {
        return subtype != null;
    }

    public void setSubtype(String subtype) {
        if (this.type == Type.IDENT)
            this.subtype = subtype;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void printMJType() {
        System.out.println(((name != null) ? name : "return type") + ": " + type.toString() +
                ((subtype != null) ? " subtype: " + subtype : ""));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if ((o instanceof MJType)) {
            if (this.type == Type.IDENT || ((MJType) o).type == Type.IDENT) {
                boolean subtypeComparison = false;
                boolean nameComparison = false;
                if (this.subtype != null && ((MJType) o).subtype != null)
                    subtypeComparison = this.subtype.equals(((MJType) o).subtype);
                if (this.name != null && ((MJType) o).name != null)
                    nameComparison = this.name.equals(((MJType) o).name);
                return  nameComparison || subtypeComparison;
            }
            return ((MJType) o).type == this.type;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 17 + name.hashCode();
        return prime * result;
    }
}
