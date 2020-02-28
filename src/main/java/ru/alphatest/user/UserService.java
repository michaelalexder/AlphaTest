package ru.alphatest.user;

import org.hibernate.Session;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * User service
 *
 *  Так как по условию в таблице БД могут быть дубликаты записей, то пришлось отказаться от нормального уникального
 *  ID. В связи с этим не получилось смаппить таблицу на сущность и пришлось работать с запросами напрямую. (Не могу представить
 *  кто может подсунуть такую убогую таблицу с дубликатами всего и нецелостными данными). Из-за этого решения код выглядит
 *  неэлегатно(((
 */
@Service
@Transactional
public class UserService {

    public static final String ID_PATTERN = "ID\\d{4}";

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    private User wrapToUser(Object[] data) {
        return User.builder().id((String) data[0]).userType(UserType.valueOf((String) data[1])).blackList((Boolean) data[2]).build();
    }

    private User fetchUserWithUserType(String id) {
        try (Session session = getSession()) {
            List users = session.createNativeQuery("select * from users where id = ?1 and user_type is not null")
                    .setParameter(1, id).list();
            if (users.isEmpty()) {
                if (isUserExists(id)) {
                    throw new RuntimeException("User with id " + id + " has no organization type");
                }
                throw new IllegalArgumentException("No user with id " + id + " exists");
            }
            return wrapToUser((Object[]) users.get(0));
        }
    }

    private boolean isUserExists(String id) {
        try (Session session = getSession()) {
            return ((BigInteger) session.createNativeQuery("select count(*) from users where id = ?1")
                    .setParameter(1, id).getSingleResult()).compareTo(BigInteger.ZERO) > 0;
        }
    }

    /**
     * Find user
     *
     * @param id user id
     */
    public RiskProfile resolveRiskProfile(@NotNull String id) {
        User user = fetchUserWithUserType(id);
        boolean isBlackList = Optional.ofNullable(user.getBlackList()).orElse(false);
        UserType userType = user.getUserType();
        switch (userType) {
            case FL: {
                if (isBlackList) {
                    return RiskProfile.RB;
                }
                return RiskProfile.RG;
            }
            case UL: {
                if (isBlackList) {
                    return RiskProfile.CB;
                }
                return RiskProfile.CG;
            }
            default:
                throw new RuntimeException("Unknown user type");
        }
    }
}
