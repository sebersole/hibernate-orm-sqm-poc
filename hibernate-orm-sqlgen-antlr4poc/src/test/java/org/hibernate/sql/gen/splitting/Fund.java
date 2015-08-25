/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.gen.splitting;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Steve Ebersole
 */
@Entity
public class Fund implements Auditable {
	@Id
	private Integer id;
	private String createdBy;
	private Instant createdAt;
	private String modifiedBy;
	private Instant modifiedAt;

	@Override
	public String getCreatedBy() {
		return createdBy;
	}

	@Override
	public Instant getCreatedAt() {
		return createdAt;
	}

	@Override
	public String getModifiedBy() {
		return modifiedBy;
	}

	@Override
	public Instant getModifiedAt() {
		return modifiedAt;
	}
}
