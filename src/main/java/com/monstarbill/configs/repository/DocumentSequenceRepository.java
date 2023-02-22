package com.monstarbill.configs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.monstarbill.configs.models.DocumentSequence;

@Repository
public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, String> {

	public Optional<DocumentSequence> findByIdAndIsDeleted(Long id, boolean isDeleted);

	public List<DocumentSequence> findBySubsidiaryIdAndTypeAndIsDeleted(Long subsidiaryId, String type, boolean isDeleted);

	public Optional<DocumentSequence> getBySubsidiaryIdAndTypeAndIsDeleted(Long subsidiaryId, String type, boolean isDeleted);
	
	@Query("SELECT c from DocumentSequence c where to_char(startDate, 'yyyy-MM-dd') <= :transactionalDate and to_char(endDate, 'yyyy-MM-dd') >= :transactionalDate and subsidiaryId = :subsidiaryId and lower(type) = lower(:type) and isDeleted = :isDeleted ")
	public Optional<DocumentSequence> findBySubsidiaryIdAndTypeAndisDeleted(
			@Param("transactionalDate") String transactionalDate, @Param("subsidiaryId") Long subsidiaryId,
			@Param("type") String type, @Param("isDeleted") boolean isDeleted);
}
