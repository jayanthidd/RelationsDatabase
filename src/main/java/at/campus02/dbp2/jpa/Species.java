package at.campus02.dbp2.jpa;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Species {

    @Id @GeneratedValue
    private Integer id;
    private String name;

    @OneToMany (mappedBy = "species", cascade = {CascadeType.REMOVE,CascadeType.PERSIST})// will always need to be a Collection
    private List<Animal> animals = new ArrayList<>();

    public List<Animal> getAnimals() {
        return animals;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
