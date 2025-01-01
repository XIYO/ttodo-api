import { todos } from '$lib/dummy.js';

export function load({params}) {
	const id = Number(params.id);

	const todo = todos.filter(todo => todo.id === id)[0];

	return {
		todo
	}
}