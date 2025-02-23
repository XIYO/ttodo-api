package point.zzicback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import point.zzicback.dto.request.CreateZzicRequest;
import point.zzicback.dto.request.UpdateZzicRequest;
import point.zzicback.dto.response.ZzicMainResponse;
import point.zzicback.domain.Zzic;
import point.zzicback.service.ZzicService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Tag(name = "ZZIC", description = "ZZIC 의 가장 기본이 되는 기능")
@RestController
@RequestMapping("/api/zzics")
@RequiredArgsConstructor
public class ZzicRestController {

    private final ZzicService zzicService;

    /**
     * ZZIC 생성
     */
    @Operation(summary = "ZZIC 생성", description = "새로운 ZZIC 항목을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공적으로 ZZIC를 생성함", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@Parameter(description = "등록할 ZZIC 정보") @Valid @RequestBody CreateZzicRequest createZzicRequest) {
        Zzic zzic = createZzicRequest.toEntity();
        this.zzicService.save(zzic);
    }

    /**
     * ZZIC 목록 조회
     */
    @Operation(summary = "ZZIC 목록 조회", description = "모든 ZZIC 항목의 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 ZZIC 목록을 조회함",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ZzicMainResponse.class))))
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ZzicMainResponse> getAll() {
        List<Zzic> zzics = this.zzicService.findAll();
        return zzics.stream()
                .map(ZzicMainResponse::fromEntity)
                .toList();
    }

    /**
     * 특정 ZZIC 조회
     */
    @Operation(summary = "특정 ZZIC 조회", description = "ID에 해당하는 ZZIC를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 ZZIC를 조회함", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ZzicMainResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 ID의 ZZIC를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Zzic getById(@Parameter(description = "조회할 ZZIC의 ID") @PathVariable Long id) {
        Optional<Zzic> zzic = this.zzicService.findById(id);
        if (zzic.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ZZIC with ID " + id + " not found");
        return zzic.get();
    }

    /**
     * ZZIC 수정
     */
    @Operation(summary = "ZZIC 수정", description = "ID에 해당하는 ZZIC를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "성공적으로 ZZIC를 수정함", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 ID의 ZZIC를 찾을 수 없음", content = @Content)
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modify(@Parameter(description = "수정할 ZZIC의 ID") @PathVariable Long id,
                       @Parameter(description = "수정할 ZZIC 정보") @Valid @RequestBody UpdateZzicRequest updateZzicRequest) {
        this.zzicService.save(updateZzicRequest.toEntity(id));
    }

    /**
     * ZZIC 삭제
     */
    @Operation(summary = "ZZIC 삭제", description = "ID에 해당하는 ZZIC를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "성공적으로 ZZIC를 삭제함", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 ID의 ZZIC를 찾을 수 없음", content = @Content)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@Parameter(description = "삭제할 ZZIC의 ID") @PathVariable Long id) {
        Optional<Zzic> zzic = this.zzicService.findById(id);
        if (zzic.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ZZIC with ID " + id + " not found");
        this.zzicService.deleteById(id);
    }
}