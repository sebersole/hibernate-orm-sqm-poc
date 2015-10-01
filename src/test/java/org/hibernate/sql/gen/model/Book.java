package org.hibernate.sql.gen.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Created by John O'Hara on 21/08/15.
 */
@Entity
public class Book {
	@Id
	private int id;

	@OneToOne
	@JoinColumn(name="id", nullable=false)
	private Author author;


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}
}
