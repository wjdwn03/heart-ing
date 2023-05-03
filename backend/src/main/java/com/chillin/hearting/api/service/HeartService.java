package com.chillin.hearting.api.service;

import com.chillin.hearting.api.data.*;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.domain.User;
import com.chillin.hearting.db.domain.UserHeart;
import com.chillin.hearting.db.repository.HeartRepository;
import com.chillin.hearting.db.repository.MessageHeartConditionRepository;
import com.chillin.hearting.db.repository.UserHeartRepository;
import com.chillin.hearting.exception.HeartNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class HeartService {

    private final HeartRepository heartRepository;
    private final UserHeartRepository userHeartRepository;
    private final MessageHeartConditionRepository messageHeartConditionRepository;

    private static final String HEART_TYPE_DEFAULT = "DEFAULT";
    private static final String HEART_TYPE_SPECIAL = "SPECIAL";
    private static final int HEART_PLANET_MAX_VALUE = 5;
    private static final int HEART_RAINBOW_MAX_VALUE = 1;
    private static final HashSet<Long> lockedHeartSet = new HashSet<>(Arrays.asList(4L, 5L));
    private static final ArrayList<Long> specialHeartList = new ArrayList<>(Arrays.asList(7L));
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 모든 도감 리스트를 반환합니다.
     * DEFAULT 타입의 도감은 잠금이 해제됩니다.
     * 로그인 사용자는 추가적으로 하트 획득 조건을 달성한 하트에 대해 잠금이 해제됩니다.
     *
     * @param user
     * @return 하트 DTO
     */
    public Data findAllHearts(User user) {
        log.debug("도감 하트 리스트 조회 - DB의 모든 하트를 조회한다.");
        List<Heart> allHearts = heartRepository.findAll();

        HashSet<Long> mySpecialHeartSet = new HashSet<>();
        if (user != null) {
            String userId = user.getId();
            log.debug("들어온 유저 아이디 : {}", userId);
            List<UserHeart> userHearts = userHeartRepository.findAllByUserId(userId);
            for (UserHeart myHeart : userHearts) {
                mySpecialHeartSet.add(myHeart.getHeart().getId());
            }
        }

        List<HeartData> resHearts = new ArrayList<>();
        for (Heart heart : allHearts) {
            resHearts.add(HeartData.of(heart, (HEART_TYPE_DEFAULT.equals(heart.getType()) || mySpecialHeartSet.contains(heart.getId()) ? false : true)));
        }
        return HeartListData.builder().heartList(resHearts).build();
    }

    /**
     * 메시지 전송용 하트 리스트를 조회합니다.
     * 기본 하트 - 모든 잠금이 해제되어있습니다. 비로그인 유저에 한해 두 개의 하트가 잠겨있습니다.
     * 스페셜 하트 - 로그인 유저 중 획득한 스페셜 하트가 제공됩니다.
     *
     * @param user
     * @return
     */
    public List<HeartData> findUserHearts(User user) {
        log.debug("메시지 전송용 하트 리스트 조회 - 기본 하트 + 내가 획득한 하트를 조회한다.");
        List<HeartData> resHearts = new ArrayList<>();
        List<Heart> findHearts = heartRepository.findAllByType(HEART_TYPE_DEFAULT);
        for (Heart heart : findHearts) {
            resHearts.add(HeartData.of(heart, false));
        }

        if (user != null) {
            String userId = user.getId();
            log.debug("들어온 유저 아이디 : {}", userId);
            List<UserHeart> myHearts = userHeartRepository.findAllByUserId(userId);
            for (UserHeart myHeart : myHearts) {
                resHearts.add(HeartData.of(myHeart.getHeart(), false));
            }
        } else {
            log.debug("비로그인 유저입니다.");
            for (HeartData heartData : resHearts) {
                if (lockedHeartSet.contains(heartData.getHeartId())) {
                    heartData.setLock();
                }
            }
        }

//        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
//        Map<String, Object> map = new HashMap<>();
//        map.put("firstName", "Gyunny");
//        map.put("lastName", "Choi");
//        map.put("gender", "Man");
//        hashOperations.putAll("key", map);

        String firstName = (String) redisTemplate.opsForHash().get("key", "firstName");
        String lastName = (String) redisTemplate.opsForHash().get("key", "lastName");
        String gender = (String) redisTemplate.opsForHash().get("key", "gender");
        System.out.println(firstName);
        System.out.println(lastName);
        System.out.println(gender);
        return resHearts;
    }

    /**
     * 도감 하트 상세보기
     * 특정 하트에 대해 하트 정보, 획득 조건에 대한 정보를 제공한다.
     * 기본 하트 - 잠금이 해제되어 있다.
     * 스페셜 하트 - 로그인 유저가 획득한 하트에 대해 잠금이 해제되어 있다. 획득하지 못한 하트에 대해서는 달성 현황 정보를 제공한다.
     *
     * @param user
     * @param heartId
     * @return
     */
    public Data findHeartDetail(User user, Long heartId) {
        Heart findHeart = heartRepository.findById(heartId).orElseThrow(HeartNotFoundException::new);
        HeartDetailData heartDetailData = HeartDetailData.of(findHeart);

        if (HEART_TYPE_DEFAULT.equals(findHeart.getType())) {
            heartDetailData.setIsLocked(false);
        } else if (HEART_TYPE_SPECIAL.equals(findHeart.getType())) {
            if (user != null) {
                String userId = user.getId();
                List<UserHeart> findUserHeart = userHeartRepository.findByHeartIdAndUserId(heartId, userId);
                if (!findUserHeart.isEmpty()) {
                    heartDetailData.setIsLocked(false);
                } else {

//                    heartDetailData.setConditions(conditionList);
                }
            }
        }

        return heartDetailData;
    }

    private void checkHeartCondition(String userId, Long heartId) {
        switch (heartId.intValue()) {
//            case 6:
//                log.debug("행성 하트 획득 조건을 반환합니다.");
//
//                HeartConditionData heartConditionData = HeartConditionData.of(null);
//                int userCurrentValue = messageHeartConditionRepository.findMaxMessageCountToSameUser(userId);
//                heartConditionData.setCurrentValue(userCurrentValue);
//                heartConditionData.setMaxValue(HEART_PLANET_MAX_VALUE);
//                heartConditionList.add(heartConditionData);
//                break;

            case 7:
                log.debug("무지개 하트 획득 조건을 반환합니다.");
                List<HeartConditionDTO> resultList = heartRepository.findDefaultHeartSentCount(userId);
                HashOperations<String, String, Map<Long, List>> hashOperations = redisTemplate.opsForHash();
                Map<Long, List> heartMap = (Map<Long, List>) hashOperations.get(userId, "conditions");
                if (heartMap == null) {
                    Map<Long, List> map = new HashMap<>();
                    map.put(heartId, new ArrayList());
                }
//                List<> heartConditions = heartMap.get(heartId);

//                for (HeartConditionDTO dto : resultList) {
//                    HeartConditionData data = HeartConditionData.builder()
//                            .heartId(dto.getHeartId())
//                            .name(dto.getName())
//                            .heartUrl(dto.getHeartUrl())
//                            .currentValue((dto.getCurrentValue() > HEART_RAINBOW_MAX_VALUE) ? HEART_RAINBOW_MAX_VALUE : dto.getCurrentValue())
//                            .maxValue(HEART_RAINBOW_MAX_VALUE)
//                            .build();
//                    heartConditionList.add(data);
//                }
                break;
        }
    }

    public void checkHeartCondition(String userId) {
        for (Long sHeartId : specialHeartList) {
            checkHeartCondition(userId, sHeartId);
        }

    }
}
