package at.campus02.dbp2.jpa;

import javax.persistence.*;

@Entity
public class Animal {

    @Id
    private String name;

    @OneToOne (cascade = CascadeType.PERSIST)
    private Student owner; // this should not take the basic default annotation

    public Student getOwner() {
        return owner;
    }

    public void setOwner(Student owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
