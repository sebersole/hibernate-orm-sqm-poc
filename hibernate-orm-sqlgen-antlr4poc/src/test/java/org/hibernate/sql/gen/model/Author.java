package org.hibernate.sql.gen.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import java.util.Set;

/**
 * Created by johara on 21/08/15.
 */
@Entity
public class Author {
	@Id
	private int id;

	@Column
	private String name;

	@OneToMany
	@JoinColumn(name="ID", nullable=false)
	private Set<Book> bookSet;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Book> getBookSet() {
		return bookSet;
	}

	public void setBookSet(Set<Book> bookSet) {
		this.bookSet = bookSet;
	}
}

