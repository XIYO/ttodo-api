<script>
	let items = ['apple', 'banana', 'orange'];

	function addItem() {
		if (!document.startViewTransition) {
			// 브라우저가 지원 안 하면 그냥 동작
			items = [...items, 'melon'];
			return;
		}

		document.startViewTransition(() => {
			// DOM을 변경하는 로직
			items = [...items, 'melon'];
		});
	}
</script>

{#snippet itemSnippet(item)}
	<div style:view-transition-name={item}>{item}</div>
{/snippet}

<button on:click={addItem}>추가하기</button>

<ul>
	{#each items as item (item)}
		{@render itemSnippet(item)}
	{/each}
</ul>
