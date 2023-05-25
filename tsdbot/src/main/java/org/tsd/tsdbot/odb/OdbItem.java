package org.tsd.tsdbot.odb;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.tsd.tsdbot.db.BaseEntity;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
public class OdbItem extends BaseEntity {

    @Column(name = "item")
    private String item;

    @ElementCollection
    @CollectionTable(name = "OdbTag", joinColumns = {@JoinColumn(name = "itemId")})
    @LazyCollection(value = LazyCollectionOption.FALSE)
//    @OnDelete(action = OnDeleteAction.CASCADE)
    @Column(name = "tag")
    private List<String> tags = new LinkedList<>();

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
