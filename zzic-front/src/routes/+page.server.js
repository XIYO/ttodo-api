import { todos, actionAdd, actionDone, actionRemove } from '$lib/dummy.js';
import { redirect } from '@sveltejs/kit';

export function load() {

	return { todos : todos.filter(todo => !todo.done),
		dones: todos.filter(todo => todo.done),
	};
}

/** @satisfies {import('./$types').Actions} */
export const actions = {
	add: async ({ request }) => {
		const data = await request.formData();
		const title = data.get('title');
		actionAdd(title);
		return { success: true };
	},
	done: async ({ request }) => {
		const data = await request.formData();
		const id = Number(data.get('id'));
		actionDone(id);
		return { success: true };
	},
	remove: async ({ request }) => {
		const data = await request.formData();
		const id = Number(data.get('id'));
		actionRemove(id);
		redirect(304, '/');
	}
};