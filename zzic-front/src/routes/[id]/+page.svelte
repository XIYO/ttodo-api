<script>
	import { page } from '$app/state';
	import { applyAction, enhance } from '$app/forms';
	import { goto } from '$app/navigation';

	const transition = () => {
		return async ({ result, update }) => {
			if (!document.startViewTransition) await applyAction(result);

			document.startViewTransition(async () => {
				if (result.type === 'redirect') {
					await goto(result.location);
				} else {
					await update();
				}
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

		<button class="remove" formaction="/?/remove">
			<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
				<path d="M9 2C8.44772 2 8 2.44772 8 3C8 3.55228 8.44772 4 9 4H15C15.5523 4 16 3.55228 16 3C16 2.44772 15.5523 2 15 2H9Z" fill="white"/>
				<path d="M10 18C9.44772 18 9 17.5523 9 17L9 11C9 10.4477 9.44772 10 10 10C10.5523 10 11 10.4477 11 11V17C11 17.5523 10.5523 18 10 18Z" fill="white"/>
				<path d="M13 17C13 17.5523 13.4477 18 14 18C14.5523 18 15 17.5523 15 17V11C15 10.4477 14.5523 10 14 10C13.4477 10 13 10.4477 13 11V17Z" fill="white"/>
				<path fill-rule="evenodd" clip-rule="evenodd" d="M2.25 6.5C2.25 5.94772 2.69772 5.5 3.25 5.5H20.75C21.3023 5.5 21.75 5.94772 21.75 6.5C21.75 7.05228 21.3023 7.5 20.75 7.5H20.0386L19.1968 19.2848C19.0473 21.3781 17.3055 22.9999 15.207 22.9999H8.7937C6.69515 22.9999 4.95339 21.3781 4.80387 19.2848L3.96209 7.5H3.25C2.69772 7.5 2.25 7.05228 2.25 6.5ZM5.96719 7.5H18.0335L17.2019 19.1424C17.1271 20.189 16.2563 20.9999 15.207 20.9999H8.7937C7.74443 20.9999 6.87354 20.189 6.79879 19.1424L5.96719 7.5Z" fill="white"/>
			</svg>
		</button>
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

				button.remove {
            width: 4rem;
            height: 5.75rem;
            padding: 0rem 1.25rem;
						margin-inline-start: .5rem;

						border:unset;
            border-radius: 1rem;
            background: #F52D65;
				}
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
