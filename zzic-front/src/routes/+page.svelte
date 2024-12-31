<script>
	/** @type {HTMLFormElement} */
	const handleFormSubmit = (event) => {
		event.preventDefault();

		// todo-input의 내용을 변수로 담아둔다.
		/** @type {string} */
		debugger
		const form = event.currentTarget; // 폼 요소를 가져옵니다.
		const formData = new FormData(form); // FormData 객체 생성
		const todo = formData.get('name-todo'); // 'todo-input'의 값을 가져옵니다.

		// todo를 찾느다.
		const ul = document.getElementById('todos');
		const li = document.createElement('li');
		li.classList.add('todo');
		ul.appendChild(li);

		const doneChecker = document.createElement('input');
		doneChecker.type = 'checkbox';
		li.appendChild(doneChecker);

		const span = document.createElement('span');
		span.textContent = todo;
		li.appendChild(span);

		const button = document.createElement('button');
		button.textContent = 'Delete';
		li.appendChild(button);
	};

	const handleDelete = (event) => {
		const li = event.currentTarget.closest('li');
		li.remove();
	};
</script>

<div class="container">
	<h1>ZZIC</h1>

<!--	지익을 입력받는 곳-->
	<form id="todo-form" onsubmit={handleFormSubmit}>
		<input type="text" name="name-todo" class="todo-input" placeholder="What needs to be done?" />
		<button type="submit" class="todo-button">Add</button>
	</form>

<!--	지익를 보여주는 곳-->
	<ul id="todos" >
		<li class="todo">
			<input type="checkbox"/><span>새해 고기 먹기</span> <button onclick={handleDelete}>Delete</button>
		</li>
	</ul>
</div>

<style>
	input[type="checkbox"]:checked + span {
		text-decoration: line-through;
	}
</style>