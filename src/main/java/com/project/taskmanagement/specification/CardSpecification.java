package com.project.taskmanagement.specification;

import com.project.taskmanagement.dto.CardSearchDto;
import com.project.taskmanagement.entity.Card;
import com.project.taskmanagement.entity.CardLabel;
import com.project.taskmanagement.entity.CardMember;
import com.project.taskmanagement.entity.TaskList;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification xử lý query động kết hợp nhiều tiêu chí cho Card (Task #38, #39, #40)
 */
public class CardSpecification {

    public static Specification<Card> filterCards(CardSearchDto filter) {
        return (Root<Card> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // RÀNG BUỘC LÕI: Luôn filter theo boardId hiện hành, KHÔNG ĐƯỢC để lọt Card của Board khác!
            if (filter == null || filter.getBoardId() == null) {
                return cb.disjunction(); // Trả về false nếu không có boardId
            }

            // Join Card -> TaskList để lọc theo taskList.boardId
            Join<Card, TaskList> taskListJoin = root.join("taskList", JoinType.INNER);
            predicates.add(cb.equal(taskListJoin.get("boardId"), filter.getBoardId()));

            // Story #38: Tìm kiếm theo tiêu đề thẻ (LIKE gần đúng, case-insensitive)
            if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                String pattern = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), pattern));
            }

            // Story #39: Tìm kiếm theo nhãn (chọn nhiều nhãn cùng lúc - IN clause)
            if (filter.getLabelIds() != null && !filter.getLabelIds().isEmpty()) {
                Join<Card, CardLabel> labelJoin = root.join("labels", JoinType.INNER);
                predicates.add(labelJoin.get("labelId").in(filter.getLabelIds()));
            }

            // Story #40: Tìm kiếm theo thành viên (chọn nhiều thành viên cùng lúc - IN clause)
            if (filter.getMemberIds() != null && !filter.getMemberIds().isEmpty()) {
                Join<Card, CardMember> memberJoin = root.join("members", JoinType.INNER);
                predicates.add(memberJoin.get("userId").in(filter.getMemberIds()));
            }

            // Loại bỏ các bản ghi trùng lặp do kết quả INNER JOIN 1-N
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
