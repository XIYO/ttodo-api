package point.zzicback.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import point.zzicback.model.Todo;

import java.util.List;

@Mapper
public interface TodoMapper {

    @Select("SELECT * FROM todo")
    List<Todo> selectAll();

    /**
     * 아래는 보편적인 MyBatis Mapper 메소드들입니다.
     * 이미 기능이 구현되어 있으며, `TodoMapper.xml` 파일에 정의되어 있습니다.
     */
    int deleteByPrimaryKey(Long id); // JPA의 deleteById와 같은 역할

    int insert(Todo record); // JPA의 save와 같은 역할

    int insertSelective(Todo record); // JPA의 save와 같은 역할

    Todo selectByPrimaryKey(Long id);  // JPA의 findById와 같은 역할

    int updateByPrimaryKeySelective(Todo record);  // JPA의 save와 같은 역할

    int updateByPrimaryKey(Todo record); // JPA의 save와 같은 역할

}