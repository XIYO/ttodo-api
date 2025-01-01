import { todos } from '$lib/dummy.js';

const actionDone = (id) => {
	todos.forEach(todo => {
		if (todo.id === id) {
			todo.done = true;
		}
	});
};

const actionRemove = (id) => {
	todos = todos.filter(todo => todo.id !== id);
};

const actionAdd = (title) => {
	todos.unshift({
		id: todos.length + 1,
		title,
		done: false
	})
};

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
		return { success: true };
	}
};