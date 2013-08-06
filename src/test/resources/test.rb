require 'java'

class Person
  attr_accessor :firstname, :lastname
  def initialize (firstname, lastname)
    @firstname = firstname
    @lastname = lastname
 end

  def fullname
    "#{firstname} #{lastname}"
  end

  def get_label
    javax.swing.JLabel.new(fullname)
  end

  def greet(person)
    person.greet("Hi, I'm #{firstname}")
  end

  def introduce(person1, person2)
    person1.greet("Hi, I'm #{firstname}. Have you met #{person2.firstname}?")
  end

  def ==(other)
    other.firstname == firstname and other.lastname == lastname
  end

  def to_s
    "Person(#{lastname}, #{firstname})"
  end

  def hash
    firstname.hash ^ lastname.hash
  end
end

module Backend
 def self.get_people
   [
     Person.new("Zaphod","Beeblebrox"),
     Person.new("Arthur","Dent"),
     Person.new("Ford","Prefect")
   ]
  end

  def self.everybody(*args)
    puts "Backend.everybody called with #{args.inspect}"
    get_people.each do |p|
      yield p
    end
  end

  def self.get_data
    { :people => get_people, :other_data => get_other_data }
  end

  def self.get_person(firstname)
    p = get_people.select { | p | p.firstname == firstname}
    if p.length > 0
      p[0]
    else
      nil
    end
  end

  def self.get_other_data
    {
      :system_name => "Deep Thought",
      :answer => 42
    }
  end
end

def get_empty_array
[]
end
