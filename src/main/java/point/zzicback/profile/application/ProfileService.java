package point.zzicback.profile.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import point.zzicback.category.infrastructure.CategoryRepository;
import point.zzicback.member.domain.Member;
import point.zzicback.member.infrastructure.persistence.MemberRepository;
import point.zzicback.profile.domain.Profile;
import point.zzicback.profile.domain.Statistics;
import point.zzicback.profile.domain.Theme;
import point.zzicback.profile.infrastructure.persistence.ProfileRepository;
import point.zzicback.profile.infrastructure.persistence.StatisticsRepository;
import point.zzicback.todo.infrastructure.persistence.TodoRepository;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {
    
    private final ProfileRepository profileRepository;
    private final StatisticsRepository statisticsRepository;
    private final TodoRepository todoRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    
    @Transactional
    public Profile createProfile(UUID memberId) {
        if (profileRepository.existsByMemberId(memberId)) {
            throw new IllegalStateException("Profile already exists for member: " + memberId);
        }
        
        Profile profile = new Profile(memberId);
        return profileRepository.save(profile);
    }
    
    public Profile getProfile(UUID memberId) {
        return profileRepository.findByMemberId(memberId)
                .orElseGet(() -> createProfile(memberId));
    }
    
    @Transactional
    public Profile saveProfile(Profile profile) {
        return profileRepository.save(profile);
    }
    
    @Transactional
    public Profile updateTheme(UUID memberId, Theme theme) {
        Profile profile = getProfile(memberId);
        profile.updateTheme(theme);
        return profileRepository.save(profile);
    }
    
    @Transactional
    public Profile updateProfileImage(UUID memberId, MultipartFile file) throws IOException {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
        
        Profile profile = getProfile(memberId);
        profile.updateProfileImage(file.getBytes(), contentType);
        
        // 이미지 URL 설정
        String imageUrl = "/members/" + memberId + "/profile/image";
        profile.updateProfileImageUrl(imageUrl);
        
        return profileRepository.save(profile);
    }
    
    @Transactional
    public Profile removeProfileImage(UUID memberId) {
        Profile profile = getProfile(memberId);
        profile.removeProfileImage();
        return profileRepository.save(profile);
    }
    
    /**
     * 사용자 통계 조회
     */
    @Transactional
    public Statistics getStatistics(UUID memberId) {
        // 실시간으로 완료한 할일 수와 카테고리 수를 계산
        long completedTodos = todoRepository.countCompletedTodosByMemberId(memberId);
        long totalCategories = categoryRepository.countByMemberId(memberId);
        
        // Statistics 엔티티에 저장/업데이트
        updateStatisticsEntity(memberId, (int) completedTodos, (int) totalCategories);
        
        return statisticsRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalStateException("Statistics not found"));
    }
    
    /**
     * Statistics 엔티티 업데이트
     */
    private void updateStatisticsEntity(UUID memberId, int completedTodos, int totalCategories) {
        Statistics statistics = statisticsRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    Member member = memberRepository.findById(memberId)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
                    return Statistics.builder()
                            .member(member)
                            .succeededTodosCount(completedTodos)
                            .categoryCount(totalCategories)
                            .build();
                });
        
        // 기존 Statistics가 있다면 업데이트
        if (statistics.getId() != null) {
            statistics.updateStatistics(completedTodos, totalCategories);
        }
        
        statisticsRepository.save(statistics);
    }
}