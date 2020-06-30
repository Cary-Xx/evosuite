package com.example;

public class Student {
	private int age;
	private int height;
	
	private Student friend;

	@Override
	public String toString() {
		return "Student [age=" + age + ", height=" + height + "]";
	}

	public int getAge() {
		return age;
	}
	
	public void setAge(int age) {
		this.age = age;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public Student getFriend() {
		return friend;
	}

	public void setFriend(Student friend) {
		this.friend = friend;
	}

//	public Student(int age, int height) {
//	super();
//	this.age = age;
//	this.height = height;
//}
}
