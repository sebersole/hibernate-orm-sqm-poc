/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.mapping;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hamcrest.CoreMatchers;

import org.hibernate.boot.MetadataSources;
import org.hibernate.persister.common.internal.PhysicalColumn;
import org.hibernate.persister.common.internal.PhysicalTable;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.DomainReferenceImplementor;
import org.hibernate.persister.common.spi.SingularAttributeImplementor;
import org.hibernate.persister.embeddable.EmbeddablePersister;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.ast.expression.AttributeReference;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.ast.select.SelectClause;
import org.hibernate.sql.ast.select.Selection;
import org.hibernate.sql.convert.spi.SelectStatementInterpreter;
import org.hibernate.sql.gen.BaseUnitTest;
import org.hibernate.sqm.query.SqmSelectStatement;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author Andrea Boriero
 */
public class EmbeddedTableSpaceGenerationTest extends BaseUnitTest {

	@Test
	public void testSelectEmbedded() {
		final SelectClause selectClause = getSelectClause( "select p.name from Person p" );
		assertThat( selectClause.getSelections().size(), is( 1 ) );

		final Selection selection = selectClause.getSelections().get( 0 );
		assertThat( selection.getResultVariable(), startsWith( "<gen:" ) );
		assertThat( selection.getSelectExpression(), instanceOf( AttributeReference.class ) );

		final AttributeReference attributeReference = (AttributeReference) selection.getSelectExpression();
		assertThat( attributeReference.getReferencedAttribute().getAttributeName(), is( "name" ) );
		checkReferenceType(
				attributeReference,
				"org.hibernate.sql.orm.internal.mapping.EmbeddedTableSpaceGenerationTest$Person",
				ImprovedEntityPersister.class
		);
		final ColumnBinding[] columnBindings = attributeReference.getColumnBindings();
		assertThat( columnBindings.length, is( 3 ) );

		final Column column1 = columnBindings[0].getColumn();
		checkPhysicalColumn( column1, "first", "PERSON" );

		final Column column2 = columnBindings[1].getColumn();
		checkPhysicalColumn( column2, "fromFather", "PERSON" );

		final Column column3 = columnBindings[2].getColumn();
		checkPhysicalColumn( column3, "fromMother", "PERSON" );
	}

	@Test
	public void testSimpleEmbeddedDereference() {
		final SelectClause selectClause = getSelectClause( "select p.name.last from Person p" );
		assertThat( selectClause.getSelections().size(), is( 1 ) );

		final Selection selection = selectClause.getSelections().get( 0 );
		assertThat( selection.getResultVariable(), startsWith( "<gen:" ) );
		assertThat( selection.getSelectExpression(), instanceOf( AttributeReference.class ) );

		final AttributeReference attributeReference = (AttributeReference) selection.getSelectExpression();
		assertThat( attributeReference.getReferencedAttribute().getAttributeName(), is( "last" ) );
		checkReferenceType(
				attributeReference,
				"org.hibernate.sql.orm.internal.mapping.EmbeddedTableSpaceGenerationTest$Person.name",
				EmbeddablePersister.class
		);

		final ColumnBinding[] columnBindings = attributeReference.getColumnBindings();
		assertThat( columnBindings.length, is( 2 ) );

		final Column column1 = columnBindings[0].getColumn();
		checkPhysicalColumn( column1, "fromFather", "PERSON" );

		final Column column2 = columnBindings[1].getColumn();
		checkPhysicalColumn( column2, "fromMother", "PERSON" );
	}

	@Test
	public void testEmbeddedDereference() {
		final SelectClause selectClause = getSelectClause( "select p.name.last.fromFather from Person p" );
		assertThat( selectClause.getSelections().size(), is( 1 ) );

		final Selection selection = selectClause.getSelections().get( 0 );
		assertThat( selection.getResultVariable(), startsWith( "<gen:" ) );
		assertThat( selection.getSelectExpression(), instanceOf( AttributeReference.class ) );

		final AttributeReference attributeReference = (AttributeReference) selection.getSelectExpression();
		assertThat( attributeReference.getReferencedAttribute().getAttributeName(), is( "fromFather" ) );
		checkReferenceType(
				attributeReference,
				"org.hibernate.sql.orm.internal.mapping.EmbeddedTableSpaceGenerationTest$Person.name.last",
				EmbeddablePersister.class
		);

		final ColumnBinding[] columnBindings = attributeReference.getColumnBindings();
		assertThat( columnBindings.length, is( 1 ) );

		final Column column1 = columnBindings[0].getColumn();
		checkPhysicalColumn( column1, "fromFather", "PERSON" );
	}

	private void checkReferenceType(AttributeReference attributeReference, String typeName, Class<? extends DomainReferenceImplementor> type) {
//		final SingularAttributeImplementor referencedAttribute = attributeReference.getReferencedAttribute();
//		final ManagedType declaringType = referencedAttribute.getDeclaringType();
//		assertThat( declaringType, instanceOf( type ) );
//		assertThat( declaringType.getTypeName(), is( typeName ) );
	}

	@Test
	public void testTwoEmbeddedOfSameType() {
		final SelectClause selectClause = getSelectClause(
				"select p.name2.last.fromFather, p.name.last.fromFather from Person p" );
		assertThat( selectClause.getSelections().size(), is( 2 ) );

		final Selection firstSelection = selectClause.getSelections().get( 0 );
		assertThat( firstSelection.getResultVariable(), startsWith( "<gen:" ) );
		assertThat( firstSelection.getSelectExpression(), instanceOf( AttributeReference.class ) );

		final AttributeReference firstAttributeReference = (AttributeReference) firstSelection.getSelectExpression();
		assertThat( firstAttributeReference.getReferencedAttribute().getAttributeName(), is( "fromFather" ) );
		checkReferenceType(
				firstAttributeReference,
				"org.hibernate.sql.orm.internal.mapping.EmbeddedTableSpaceGenerationTest$Person.name2.last",
				EmbeddablePersister.class
		);

		final ColumnBinding[] columnBindings = firstAttributeReference.getColumnBindings();
		assertThat( columnBindings.length, is( 1 ) );

		checkPhysicalColumn( columnBindings[0].getColumn(), "name2FromFather", "PERSON" );

		final Selection secondSelection = selectClause.getSelections().get( 1 );
		assertThat( secondSelection.getResultVariable(), startsWith( "<gen:" ) );
		assertThat( secondSelection.getSelectExpression(), instanceOf( AttributeReference.class ) );

		final AttributeReference secondSelectionAttributeReference = (AttributeReference) secondSelection.getSelectExpression();
		assertThat( secondSelectionAttributeReference.getReferencedAttribute().getAttributeName(), is( "fromFather" ) );
		checkReferenceType(
				secondSelectionAttributeReference,
				"org.hibernate.sql.orm.internal.mapping.EmbeddedTableSpaceGenerationTest$Person.name.last",
				EmbeddablePersister.class
		);

		final ColumnBinding[] secondSelectionColumnBindings = secondSelectionAttributeReference.getColumnBindings();
		assertThat( secondSelectionColumnBindings.length, is( 1 ) );

		checkPhysicalColumn( secondSelectionColumnBindings[0].getColumn(), "fromFather", "PERSON" );
	}

	@Test
	public void testTwoEmbeddedOfSameType2() {
		final SelectClause selectClause = getSelectClause(
				"select p.name2.last.fromFather, p.name.last from Person p" );
		assertThat( selectClause.getSelections().size(), is( 2 ) );

		final Selection firstSelection = selectClause.getSelections().get( 0 );
		assertThat( firstSelection.getResultVariable(), startsWith( "<gen:" ) );
		assertThat( firstSelection.getSelectExpression(), instanceOf( AttributeReference.class ) );

		final AttributeReference firstAttributeReference = (AttributeReference) firstSelection.getSelectExpression();
		assertThat( firstAttributeReference.getReferencedAttribute().getAttributeName(), is( "fromFather" ) );
		checkReferenceType(
				firstAttributeReference,
				"org.hibernate.sql.orm.internal.mapping.EmbeddedTableSpaceGenerationTest$Person.name2.last",
				EmbeddablePersister.class
		);

		final ColumnBinding[] columnBindings = firstAttributeReference.getColumnBindings();
		assertThat( columnBindings.length, is( 1 ) );

		checkPhysicalColumn( columnBindings[0].getColumn(), "name2FromFather", "PERSON" );

		final Selection secondSelection = selectClause.getSelections().get( 1 );
		assertThat( secondSelection.getResultVariable(), startsWith( "<gen:" ) );
		assertThat( secondSelection.getSelectExpression(), instanceOf( AttributeReference.class ) );

		final AttributeReference secondSelectionAttributeReference = (AttributeReference) secondSelection.getSelectExpression();
		assertThat( secondSelectionAttributeReference.getReferencedAttribute().getAttributeName(), is( "last" ) );
		checkReferenceType(
				secondSelectionAttributeReference,
				"org.hibernate.sql.orm.internal.mapping.EmbeddedTableSpaceGenerationTest$Person.name",
				EmbeddablePersister.class
		);

		final ColumnBinding[] secondSelectionColumnBindings = secondSelectionAttributeReference.getColumnBindings();
		assertThat( secondSelectionColumnBindings.length, is( 2 ) );

		checkPhysicalColumn( secondSelectionColumnBindings[0].getColumn(), "fromFather", "PERSON" );
		checkPhysicalColumn( secondSelectionColumnBindings[1].getColumn(), "fromMother", "PERSON" );
	}

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		metadataSources.addAnnotatedClass( Person.class );
	}

	@Entity(name = "Person")
	@javax.persistence.Table(name = "PERSON")
	public static class Person {
		@Id
		@GeneratedValue
		long id;

		@Embedded
		Name name;

		@Embedded
		@AttributeOverrides(value = {
				@AttributeOverride(name = "first", column = @javax.persistence.Column(name = "name2First")),
				@AttributeOverride(name = "last.fromFather", column = @javax.persistence.Column(name = "name2FromFather")),
				@AttributeOverride(name = "last.fromMother", column = @javax.persistence.Column(name = "name2FromMother"))
		}
		)
		Name name2;
	}

	@Embeddable
	public static class Name {
		String first;

		@Embedded
		Surname last;
	}

	@Embeddable
	public static class Surname {
		String fromFather;
		String fromMother;
	}

	private SelectClause getSelectClause(String query) {
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( query );

		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOptions(), callBack() );
		interpreter.interpret( statement );

		return interpreter.getSelectQuery().getQuerySpec().getSelectClause();
	}

	private void checkPhysicalColumn(Column column, String columnName, String columnTableName) {
		assertThat( column, CoreMatchers.instanceOf( PhysicalColumn.class ) );
		assertThat( ((PhysicalColumn) column).getName(), is( columnName ) );
		assertThat( ((PhysicalTable) column.getSourceTable()).getTableName(), is( columnTableName ) );
	}
}
