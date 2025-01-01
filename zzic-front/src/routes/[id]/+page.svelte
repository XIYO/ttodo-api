<script>
	import { page } from '$app/state';
	import { applyAction, enhance } from '$app/forms';

	const transition = () => {
		return async ({ result, update }) => {
			if (!document.startViewTransition) await applyAction(result);

			document.startViewTransition(async () => {
				// DOM을 변경하는 로직
				await update();
			});
		};
	};
</script>

{#snippet zzic(data)}
	<form method="POST" use:enhance={transition} class="todo"
				style:view-transition-name={['zzic',data.id].join('-')}
	>
		<input type="hidden" name="id" bind:value={data.id} />
		<button type="submit" class="zzic-left"
						data-done={data.done}
						style:visibility={data.done ? 'hidden' : 'visible'}
						formaction="/?/done">
			<svg xmlns="http://www.w3.org/2000/svg" width="46" height="46" viewBox="0 0 46 46" fill="none">
				<path
					d="M23 45C35.1503 45 45 35.1503 45 23C45 10.8497 35.1503 1 23 1C10.8497 1 1 10.8497 1 23C1 35.1503 10.8497 45 23 45Z"
					stroke="#E2E4E8" stroke-width="2" stroke-miterlimit="10" />
			</svg>

		</button>

		<div class="zzic-right">
			<div class="container">
				<div>{data.title}</div>
			</div>
		</div>
	</form>
{/snippet}

<h1 style:view-transition-name="h1">ZZIC</h1>

<div id="content"
		 style:view-transition-name={['todo', page.data.todo.id].join('-')}
>
	{@render zzic(page.data.todo)}
</div>


<style>

    .todo {
        display: grid;
        grid-template-columns: auto 1fr auto;

        a {
            text-decoration: unset;
        }
    }

    #todos {
        display: flex;
        flex-direction: column;
        gap: .25rem;
    }

    .zzic-left {
        /* reset */
        background-color: unset;

        cursor: pointer;

        /* 보더 이미지가 찌그러지기 안기 위한 사이즈 설정 */
        block-size: 90px;
        min-inline-size: 90px;

        border: 15px solid transparent; /* 기본 테두리 두께 */
        border-image-source: url('zzic-left.svg'); /* 좌우 반전된 zzic-left 이미지 사용 */
        border-image-slice: 15 15 15 15 fill; /* 상, 우, 하, 좌 각각 15픽셀 슬라이스 + 중앙 영역 채우기 */
        border-image-repeat: stretch; /* 가로는 stretch, 세로는 repeat */
        box-sizing: border-box;
    }

    .zzic-right {
        /* reset */
        overflow: auto;
        white-space: nowrap;

        color: white;

        /* 보더 이미지가 찌그러지기 안기 위한 사이즈 설정 */
        block-size: 90px;
        min-inline-size: 90px;

        border: 15px solid transparent; /* 기본 테두리 두께 */
        border-image-source: url('zzic-right.svg'); /* 좌우 반전된 zzic-left 이미지 사용 */
        border-image-slice: 15 15 15 15 fill; /* 상, 우, 하, 좌 각각 15픽셀 슬라이스 + 중앙 영역 채우기 */
        border-image-repeat: stretch; /* 가로는 stretch, 세로는 repeat */
        box-sizing: border-box;

        .container {
            display: flex;
            justify-content: space-between;

            button {
                /* reset */
                background-color: unset;
                padding: unset;
                border: unset;

                /* style */
                cursor: pointer;
            }
        }
    }
</style>
