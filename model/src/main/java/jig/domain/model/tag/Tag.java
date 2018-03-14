package jig.domain.model.tag;

public enum Tag {
    SERVICE,
    REPOSITORY,
    ENUM,
    ENUM_PARAMETERIZED {
        @Override
        public boolean matches(Tag tag) {
            return ENUM == tag || super.matches(tag);
        }
    },
    ENUM_POLYMORPHISM {
        @Override
        public boolean matches(Tag tag) {
            return ENUM == tag || super.matches(tag);
        }
    },
    ENUM_BEHAVIOUR {
        @Override
        public boolean matches(Tag tag) {
            return ENUM == tag || super.matches(tag);
        }
    },
    IDENTIFIER,
    NUMBER,
    DATE,
    TERM,
    COLLECTION;

    public boolean matches(Tag tag) {
        return this == tag;
    }
}
